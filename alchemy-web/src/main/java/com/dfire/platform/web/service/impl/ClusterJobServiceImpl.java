package com.dfire.platform.web.service.impl;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.dfire.platform.web.cluster.request.ListJobFlinkRequest;
import com.dfire.platform.web.cluster.response.ListJobFlinkResponse;
import com.dfire.platform.web.cluster.response.ListJobResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.runtime.client.JobStatusMessage;
import org.apache.flink.runtime.jobgraph.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.dfire.platform.web.cluster.ClusterManager;
import com.dfire.platform.web.cluster.request.JarSubmitFlinkRequest;
import com.dfire.platform.web.cluster.request.SqlSubmitFlinkRequest;
import com.dfire.platform.web.cluster.request.SubmitRequest;
import com.dfire.platform.web.cluster.response.Response;
import com.dfire.platform.web.cluster.response.SubmitFlinkResponse;
import com.dfire.platform.web.common.*;
import com.dfire.platform.web.configuration.Flame;
import com.dfire.platform.web.data.*;
import com.dfire.platform.web.descriptor.*;
import com.dfire.platform.web.service.ClusterJobService;
import com.dfire.platform.web.util.JsonUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.twodfire.redis.ICacheService;

/**
 * @author congbai
 * @date 2018/6/19
 */
@SuppressWarnings("AlibabaThreadPoolCreation")
@Service
public class ClusterJobServiceImpl implements ClusterJobService, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterJobServiceImpl.class);
    private final ClusterManager clusterManager;
    private final DescriptorManager descriptorManager;
    private final AcJobRepository jobRepository;
    private final AcJobConfRepository jobConfRepository;
    private final AcJobHistoryRepository jobHistoryRepository;
    private final Flame flame;
    private final ICacheService cacheService;
    private AtomicBoolean shutDown = new AtomicBoolean(false);
    private BlockingQueue<Long> queue;

    private ExecutorService submitService;

    private ScheduledExecutorService statusService;

    private int queueSize;

    private long initialDelay;

    private long period;

    public ClusterJobServiceImpl(ClusterManager clusterManager,
                                 DescriptorManager descriptorManager,
                                 AcJobRepository jobRepository,
                                 AcJobConfRepository jobConfRepository,
                                 AcJobHistoryRepository jobHistoryRepository,
                                 Flame flame,
                                 ICacheService cacheService) {
        this.clusterManager = clusterManager;
        this.descriptorManager = descriptorManager;
        this.jobRepository = jobRepository;
        this.jobConfRepository = jobConfRepository;
        this.jobHistoryRepository = jobHistoryRepository;
        this.flame = flame;
        this.cacheService = cacheService;
    }

    @Override
    public void submit(Long id) {
        try {
            this.queue.put(id);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.queue = new ArrayBlockingQueue<>(queueSize);
        this.submitService = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("cluster-job-submit-%d").build());
        this.statusService = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("cluster-job-status-%d").build());
        initSubmitTask();
        this.submitService.submit(new SubmitTask());
        this.statusService.scheduleAtFixedRate(new StatusTask(), initialDelay, period, TimeUnit.MINUTES);
    }

    private void initSubmitTask() throws InterruptedException {
        AcJob acJob = new AcJob();
        acJob.setStatus(Status.AUDIT_PASS.getStatus());
        acJob.setIsValid(Valid.VALID.getValid());
        List<AcJob> acJobList = this.jobRepository.findAll(Example.of(acJob));
        if (CollectionUtils.isEmpty(acJobList)) {
            return;
        }
        for (AcJob job : acJobList) {
            this.queue.put(job.getId());
        }
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    class SubmitTask implements Runnable {

        @Override
        public void run() {
            while (!shutDown.get()) {
                try {
                    Long id = queue.take();
                    submitRequest(id);
                } catch (InterruptedException e) {
                    shutDown.set(true);
                    LOGGER.info("The thread {} is interrupted", Thread.currentThread().getName());
                    break;
                }
            }
        }

        private void submitRequest(Long id) {
            String key = StringUtils.join(Constants.SUBMIT_LOCK_PREFIX, id);
            if (cacheService.setnx(key, Constants.SUBMIT_LOCK_TIME, key) == 0) {
                return;
            }
            try {
                AcJob acJob = jobRepository.findOne(id);
                if (acJob == null) {
                    LOGGER.warn("job doesn't exist,id:{}", id);
                    return;
                }
                if (Status.AUDIT_PASS.getStatus() != acJob.getStatus()) {
                    LOGGER.warn("can't sumbit job:{} ,because job status is {}", id, acJob.getStatus());
                    return;
                }
                ClusterType clusterType = clusterManager.getClusterType(acJob.getCluster());
                if (clusterType == null) {
                    LOGGER.warn("clusterType is null,id:{},cluster:{}", id, acJob.getCluster());
                    return;
                }
                List<AcJobConf> jobConfs = findJobConf(id);
                if (CollectionUtils.isEmpty(jobConfs)) {
                    LOGGER.warn("jobConfs is empty,id:{}", id);
                    return;
                }
                SubmitRequest submitRequest = createSubmitRequest(acJob, jobConfs, clusterType);
                if (submitRequest == null) {
                    LOGGER.warn("submitRequest is null,id:{}", id);
                    updateJobStatus(acJob, Status.FAILED.getStatus());
                    return;
                }
                Response response = clusterManager.send(submitRequest);
                if (response.isSuccess()) {
                    SubmitFlinkResponse submitResponse = (SubmitFlinkResponse)response;
                    insertJobHistory(acJob, submitResponse);
                    updateJobStatus(acJob, Status.COMMIT.getStatus());
                } else {
                    updateJobStatus(acJob, Status.FAILED.getStatus());
                }
            } finally {
                cacheService.del(key);
            }
        }

        private void updateJobStatus(AcJob acJob, int status) {
            acJob.setStatus(status);
            jobRepository.saveAndFlush(acJob);
        }

        private void insertJobHistory(AcJob acJob, SubmitFlinkResponse submitResponse) {
            jobHistoryRepository.deleteJobHistory(acJob.getId());
            AcJobHistory acJobHistory = new AcJobHistory();
            acJobHistory.setId(flame.nextId());
            acJobHistory.setAcJobId(acJob.getId());
            acJobHistory.setClusterJobId(submitResponse.getJobId());
            acJobHistory.setCreateTime(new Date());
            jobHistoryRepository.save(acJobHistory);
        }

        private SubmitRequest createSubmitRequest(AcJob acJob, List<AcJobConf> jobConfs, ClusterType clusterType) {
            SubmitRequest submitRequest = null;
            if (SubmitMode.SQL.getMode() == acJob.getSubmitMode()) {
                if (ClusterType.FLINK.equals(clusterType)) {
                    submitRequest = createSqlSubmitFlinkRequest(acJob, jobConfs);
                }
            } else if (SubmitMode.JAR.getMode() == acJob.getSubmitMode()) {
                if (ClusterType.FLINK.equals(clusterType)) {
                    submitRequest = createJarSubmitFlinkRequest(acJob, jobConfs);
                }
            }
            return submitRequest;
        }

        private JarSubmitFlinkRequest createJarSubmitFlinkRequest(AcJob acJob, List<AcJobConf> jobConfs) {
            JarSubmitFlinkRequest jarSubmitFlinkRequest = new JarSubmitFlinkRequest();
            jarSubmitFlinkRequest.setCluster(acJob.getCluster());
            jarSubmitFlinkRequest.setJobName(acJob.getName());
            AcJobConf acJobConf = jobConfs.get(0);
            if (ConfType.JAR.getType() != acJobConf.getType()) {
                LOGGER.warn("job hasn't jar conf,acJobId:{},acJobConfId:{}", acJob.getId(), acJobConf.getId());
                return null;
            }
            JarInfoDescriptor descriptor = createDescriptor(acJobConf, JarInfoDescriptor.class);
            downLoadJarIfNeed(descriptor);
            jarSubmitFlinkRequest.setJarInfoDescriptor(descriptor);
            return jarSubmitFlinkRequest;
        }

        /**
         * 从远程文件服务器下载jar包,直接从网上copy的，罪过罪过
         * 
         * @param descriptor
         */
        private void downLoadJarIfNeed(JarInfoDescriptor descriptor) {
            File file = new File(descriptor.getJarPath());
            if (file.exists()) {
                return;
            }
            URL urlfile;
            HttpURLConnection httpUrl;
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            File f = new File(descriptor.getJarPath());
            try {
                urlfile = new URL(descriptor.getRemoteUrl());
                httpUrl = (HttpURLConnection)urlfile.openConnection();
                httpUrl.connect();
                bis = new BufferedInputStream(httpUrl.getInputStream());
                bos = new BufferedOutputStream(new FileOutputStream(f));
                int len = 2048;
                byte[] b = new byte[len];
                while ((len = bis.read(b)) != -1) {
                    bos.write(b, 0, len);
                }
                bos.flush();
                bis.close();
                httpUrl.disconnect();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            } finally {
                try {
                    bis.close();
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private SqlSubmitFlinkRequest createSqlSubmitFlinkRequest(AcJob acJob, List<AcJobConf> jobConfs) {
            SqlSubmitFlinkRequest sqlSubmitFlinkRequest = new SqlSubmitFlinkRequest();
            sqlSubmitFlinkRequest.setCluster(acJob.getCluster());
            sqlSubmitFlinkRequest.setJobName(acJob.getName());
            List<SourceDescriptor> inputs = new ArrayList<>(jobConfs.size());
            List<UdfDescriptor> userDefineFunctions = new ArrayList<>(jobConfs.size());
            List<SinkDescriptor> outputs = new ArrayList<>(jobConfs.size());
            for (AcJobConf acJobConf : jobConfs) {
                switch (ConfType.fromType(acJobConf.getType())) {
                    case SQL:
                        SqlInfoDescriptor descriptor = createDescriptor(acJobConf, SqlInfoDescriptor.class);
                        sqlSubmitFlinkRequest.setSqlInfoDescriptor(descriptor);
                        break;
                    case SOURCE:
                        SourceDescriptor sourceDescriptor = createDescriptor(acJobConf, null);
                        inputs.add(sourceDescriptor);
                        break;
                    case UDF:
                        UdfDescriptor udfDescriptor = createDescriptor(acJobConf, null);
                        userDefineFunctions.add(udfDescriptor);
                        break;
                    case SINK:
                        SinkDescriptor sinkDescriptor = createDescriptor(acJobConf, null);
                        outputs.add(sinkDescriptor);
                        break;
                    default:
                        // nothing to do
                }
            }
            sqlSubmitFlinkRequest.setInputs(inputs);
            sqlSubmitFlinkRequest.setUserDefineFunctions(userDefineFunctions);
            sqlSubmitFlinkRequest.setOutputs(outputs);
            return sqlSubmitFlinkRequest;
        }

        private <T> T createDescriptor(AcJobConf acJobConf, Class<? extends T> clazz) {
            DescriptorData descriptorData = JsonUtils.fromJson(acJobConf.getContent(), DescriptorData.class);
            if (clazz == null) {
                Descriptor prototype = descriptorManager.getByContentType(descriptorData.getContentType());
                clazz = (Class<? extends T>)prototype.getClass();
            }
            return JsonUtils.fromJson(descriptorData.getDescriptor(), clazz);
        }

        private List<AcJobConf> findJobConf(Long id) {
            AcJobConf acJobConf = new AcJobConf();
            acJobConf.setIsValid(Valid.VALID.getValid());
            acJobConf.setAcJobId(id);
            return jobConfRepository.findAll(Example.of(acJobConf));
        }
    }

    class StatusTask implements Runnable {

        private AtomicLong count = new AtomicLong();

        @Override
        public void run() {
            // 为了防止redis锁期间，不能执行任务，所以增加了count
            try {
                count.incrementAndGet();
                Object value = cacheService.get(Constants.STATUS_KEY);
                // 判断redis是否存在值
                // 如果不存在，则抢占执行的权利
                if (value == null) {
                    updateJobStatusIfHasLock();
                    return;
                }
                // 如果存在，判断值是否大于本地值：
                // 大于或等于本地值，将本地值更新为redis的值，放弃这次的执行权
                // 小于本地值，抢占执行的权利
                long longValue = Long.valueOf(String.valueOf(value));
                if (longValue >= count.longValue()) {
                    count.set(longValue);
                } else {
                    updateJobStatusIfHasLock();
                }

            } catch (Exception e) {
                shutDown.set(true);
                LOGGER.info("The thread {} is interrupted", Thread.currentThread().getName());
            }
        }

        private void updateJobStatusIfHasLock() {
            if (!hasLock()) {
                return;
            }
            Set<String> clusters=clusterManager.clusters();
            for(String cluster:clusters){
                ClusterType clusterType=clusterManager.getClusterType(cluster);
                if(ClusterType.FLINK.equals(clusterType)){
                    Response response=clusterManager.send(new ListJobFlinkRequest(cluster));
                    if(!response.isSuccess()){
                        continue;
                    }
                    ListJobFlinkResponse flinkResponse= (ListJobFlinkResponse) response;
                    for (JobStatusMessage jobStatusMessage : flinkResponse.getJobStatusMessages()) {
                        AcJobHistory acJobHistory=jobHistoryRepository.findByClusterJobId(jobStatusMessage.getJobId().toString(),
                                new PageRequest(0,1,new Sort(Sort.Direction.DESC,"update_time")));
                        switch (jobStatusMessage.getJobState()){
                            case CREATED:
                            case RESTARTING:
                                break;
                            case RUNNING:
                                updateJobStatus(acJobHistory.getAcJobId(),Status.RUNNING.getStatus());
                                break;
                            case FAILING:
                            case FAILED:
                                updateJobStatus(acJobHistory.getAcJobId(),Status.FAILED.getStatus());
                                break;
                            case CANCELLING:
                            case CANCELED:
                                updateJobStatus(acJobHistory.getAcJobId(),Status.CANCELED.getStatus());
                                break;
                            case FINISHED:
                                updateJobStatus(acJobHistory.getAcJobId(),Status.FINISHED.getStatus());
                                break;
                            case SUSPENDED:
                            case RECONCILING:
                                default:
                                    //nothing to  do
                        }
                    }
                }
            }
            jobRepository.flush();
        }

        private void updateJobStatus(Long acJobId, int status) {
            AcJob acJob=jobRepository.findOne(acJobId);
            if(Status.RUNNING.getStatus()==acJob.getStatus()||Status.COMMIT.getStatus()!=acJob.getStatus()){
                acJob.setStatus(status);
                jobRepository.save(acJob);
            }else{
                LOGGER.warn("job :{} status is :{},but flink status is :{}",acJobId,acJob.getStatus(),status);
            }
        }

        private boolean hasLock() {
            return cacheService.setnx(StringUtils.join(Constants.STATUS_LOCK_PREFIX, count.toString()),
                Constants.STATUS_LOCK_TIME, count.toString()) == 1;
        }
    }
}
