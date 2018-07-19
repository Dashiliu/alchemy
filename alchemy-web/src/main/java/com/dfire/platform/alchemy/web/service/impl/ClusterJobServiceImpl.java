package com.dfire.platform.alchemy.web.service.impl;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.dfire.platform.alchemy.web.bind.BindPropertiesFactory;
import com.dfire.platform.alchemy.web.cluster.ClusterManager;
import com.dfire.platform.alchemy.web.cluster.request.*;
import com.dfire.platform.alchemy.web.cluster.response.JobStatusResponse;
import com.dfire.platform.alchemy.web.cluster.response.Response;
import com.dfire.platform.alchemy.web.cluster.response.SubmitFlinkResponse;
import com.dfire.platform.alchemy.web.common.*;
import com.dfire.platform.alchemy.web.config.Flame;
import com.dfire.platform.alchemy.web.descriptor.JarInfoDescriptor;
import com.dfire.platform.alchemy.web.descriptor.TableDescriptor;
import com.dfire.platform.alchemy.web.domain.AcJob;
import com.dfire.platform.alchemy.web.domain.AcJobConf;
import com.dfire.platform.alchemy.web.domain.AcJobHistory;
import com.dfire.platform.alchemy.web.repository.AcJobConfRepository;
import com.dfire.platform.alchemy.web.repository.AcJobHistoryRepository;
import com.dfire.platform.alchemy.web.repository.AcJobRepository;
import com.dfire.platform.alchemy.web.service.ClusterJobService;
import com.dfire.platform.alchemy.web.util.JsonUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.twodfire.redis.ICacheService;

/**
 * @author congbai
 * @date 2018/6/19
 */
@SuppressWarnings("AlibabaThreadPoolCreation")
@Service
@ConfigurationProperties(prefix = "alchemy.job")
public class ClusterJobServiceImpl implements ClusterJobService, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterJobServiceImpl.class);
    private final ClusterManager clusterManager;
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

    public ClusterJobServiceImpl(ClusterManager clusterManager, AcJobRepository jobRepository,
        AcJobConfRepository jobConfRepository, AcJobHistoryRepository jobHistoryRepository, Flame flame,
        ICacheService cacheService) {
        this.clusterManager = clusterManager;
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
                } catch (Throwable e) {
                    LOGGER.error("The thread {} has exception", Thread.currentThread().getName(), e);
                }
            }
        }

        private void submitRequest(Long id) {
            String key = StringUtils.join(Constants.SUBMIT_LOCK_PREFIX, id);
            if (cacheService.setnx(key, Constants.SUBMIT_LOCK_TIME, key) == 0) {
                return;
            }
            try {
                Optional<AcJob> acJob = jobRepository.findById(id);
                if (!acJob.isPresent()) {
                    LOGGER.warn("job doesn't exist,id:{}", id);
                    return;
                }
                if (Status.AUDIT_PASS.getStatus() != acJob.get().getStatus()) {
                    LOGGER.warn("can't sumbit job:{} ,because job status is {}", id, acJob.get().getStatus());
                    return;
                }
                ClusterType clusterType = clusterManager.getClusterType(acJob.get().getCluster());
                if (clusterType == null) {
                    LOGGER.warn("clusterType is null,id:{},cluster:{}", id, acJob.get().getCluster());
                    return;
                }
                cancelJob(acJob.get(), clusterType);
                SubmitRequest submitRequest = createSubmitRequest(acJob.get(), clusterType);
                if (submitRequest == null) {
                    LOGGER.warn("submitRequest is null,id:{}", id);
                    updateJobStatus(acJob.get(), Status.FAILED.getStatus());
                    return;
                }
                Response response = clusterManager.send(submitRequest);
                if (response.isSuccess()) {
                    SubmitFlinkResponse submitResponse = (SubmitFlinkResponse)response;
                    insertJobHistory(acJob.get(), submitResponse);
                    updateJobStatus(acJob.get(), Status.COMMIT.getStatus());
                } else {
                    updateJobStatus(acJob.get(), Status.FAILED.getStatus());
                }
            } catch (Exception e) {
                LOGGER.error("submitRequest,id:{}", id, e);
            } finally {
                cacheService.del(key);
            }
        }

        private void cancelJob(AcJob acJob, ClusterType clusterType) {
            List<AcJobHistory> acJobHistorys = jobHistoryRepository.findByAcJobId(acJob.getId(),
                new PageRequest(0, 1, new Sort(Sort.Direction.DESC, "updateTime")));
            if (CollectionUtils.isEmpty(acJobHistorys)) {
                return;
            }
            AcJobHistory acJobHistory = acJobHistorys.get(0);
            if (ClusterType.FLINK.equals(clusterType)) {
                clusterManager.send(new CancelFlinkRequest(acJobHistory.getClusterJobId(), acJob.getCluster()));
            }
            jobHistoryRepository.deleteJobHistory(acJob.getId());
        }

        private void updateJobStatus(AcJob acJob, int status) {
            acJob.setStatus(status);
            jobRepository.saveAndFlush(acJob);
        }

        private void insertJobHistory(AcJob acJob, SubmitFlinkResponse submitResponse) {
            AcJobHistory acJobHistory = new AcJobHistory();
            acJobHistory.setId(flame.nextId());
            acJobHistory.setAcJobId(acJob.getId());
            acJobHistory.setClusterJobId(submitResponse.getJobId());
            acJobHistory.setCreateTime(new Date());
            jobHistoryRepository.save(acJobHistory);
        }

        private SubmitRequest createSubmitRequest(AcJob acJob, ClusterType clusterType) throws Exception {
            List<AcJobConf> jobConfs = findJobConf(acJob.getId());
            if (CollectionUtils.isEmpty(jobConfs)) {
                LOGGER.warn("jobConfs is empty,id:{}", acJob.getId());
                return null;
            }
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
            for (AcJobConf acJobConf : jobConfs) {
                if (ConfType.JAR.getType() != acJobConf.getType()) {
                    continue;
                }
                JarInfoDescriptor descriptor = buildJar(acJobConf, JarInfoDescriptor.class);
                downLoadJarIfNeed(descriptor);
                jarSubmitFlinkRequest.setJarInfoDescriptor(descriptor);
            }
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

        private SqlSubmitFlinkRequest createSqlSubmitFlinkRequest(AcJob acJob, List<AcJobConf> jobConfs)
            throws Exception {
            SqlSubmitFlinkRequest sqlSubmitFlinkRequest = new SqlSubmitFlinkRequest();
            TableDescriptor tableDescriptor = new TableDescriptor();
            sqlSubmitFlinkRequest.setTable(tableDescriptor);
            for (AcJobConf acJobConf : jobConfs) {
                switch (ConfType.fromType(acJobConf.getType())) {
                    case SQL:
                        bindSql(acJobConf, sqlSubmitFlinkRequest);
                        break;
                    case CONFIG:
                        bindConfig(acJobConf, sqlSubmitFlinkRequest);
                        break;
                    default:
                        // nothing to do
                }
            }
            sqlSubmitFlinkRequest.setCluster(acJob.getCluster());
            sqlSubmitFlinkRequest.setJobName(acJob.getName());
            return sqlSubmitFlinkRequest;
        }

        private void bindConfig(AcJobConf acJobConf, SqlSubmitFlinkRequest submitFlinkRequest) throws Exception {
            Content content = JsonUtils.fromJson(acJobConf.getContent(), Content.class);
            BindPropertiesFactory.bindProperties(submitFlinkRequest, Constants.BIND_PREFIX, content.getConfig());
            submitFlinkRequest.getTable().setCodes(content.getCode());
        }

        private void bindSql(AcJobConf acJobConf, SqlSubmitFlinkRequest submitFlinkRequest) {
            Content content = JsonUtils.fromJson(acJobConf.getContent(), Content.class);
            submitFlinkRequest.getTable().setSql(content.getConfig());
        }

        private <T> T buildJar(AcJobConf acJobConf, Class<? extends T> clazz) {
            Content content = JsonUtils.fromJson(acJobConf.getContent(), Content.class);
            return JsonUtils.fromJson(content.getConfig(), clazz);
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
                // 如3果存在，判断值是否大于本地值：
                // 大于或等于本地值，将本地值更新为redis的值，放弃这次的执行权
                // 小于本地值，抢占执行的权利
                long longValue = Long.valueOf(String.valueOf(value));
                if (longValue >= count.longValue()) {
                    count.set(longValue);
                } else {
                    updateJobStatusIfHasLock();
                }

            } catch (Exception e) {
                LOGGER.error("The thread {} has exception", Thread.currentThread().getName(), e);
            }
        }

        private void updateJobStatusIfHasLock() {
            if (!hasLock()) {
                return;
            }
            Pageable pageable = PageRequest.of(0, Constants.PAGE_SIZE);
            updateJobStatusByPage(pageable);
        }

        private void updateJobStatusByPage(Pageable pageable) {
            Page<AcJobHistory> acJobHistories = jobHistoryRepository.findByIsValid(Valid.VALID.getValid(), pageable);
            acJobHistories.getContent().forEach(acJobHistory -> {
                Optional<AcJob> acJobOptional = jobRepository.findById(acJobHistory.getAcJobId());
                if (!acJobOptional.isPresent()) {
                    return;
                }
                Response response = clusterManager
                    .send(new JobStatusRequest(acJobOptional.get().getCluster(), acJobHistory.getClusterJobId()));
                if (!response.isSuccess()) {
                    return;
                }
                JobStatusResponse flinkResponse = (JobStatusResponse)response;
                if (acJobOptional.get().getStatus().intValue() != flinkResponse.getStatus().intValue()) {
                    updateJobStatus(acJobOptional.get().getId(), flinkResponse.getStatus());
                }
            });
            if (acJobHistories.hasNext()) {
                updateJobStatusByPage(acJobHistories.nextPageable());
            }
            jobRepository.flush();
        }

        private void updateJobStatus(Long acJobId, int status) {
            Optional<AcJob> acJob = jobRepository.findById(acJobId);
            if (Status.AUDIT_FAIL.getStatus() < acJob.get().getStatus()) {
                acJob.get().setStatus(status);
                jobRepository.save(acJob.get());
            } else {
                LOGGER.warn("job :{} status is :{},but flink status is :{}", acJobId, acJob.get().getStatus(), status);
            }
        }

        private boolean hasLock() {
            return cacheService.setnx(StringUtils.join(Constants.STATUS_LOCK_PREFIX, count.toString()),
                Constants.STATUS_LOCK_TIME, count.toString()) == 1;
        }
    }
}
