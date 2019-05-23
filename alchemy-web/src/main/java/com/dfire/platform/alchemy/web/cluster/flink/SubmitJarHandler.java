package com.dfire.platform.alchemy.web.cluster.flink;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.flink.api.common.JobSubmissionResult;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.optimizer.DataStatistics;
import org.apache.flink.optimizer.Optimizer;
import org.apache.flink.optimizer.costs.DefaultCostEstimator;
import org.apache.flink.optimizer.plan.FlinkPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dfire.platform.alchemy.web.cluster.Handler;
import com.dfire.platform.alchemy.web.util.FileUtils;
import com.dfire.platform.alchemy.web.util.JarArgUtils;
import com.dfire.platform.alchemy.web.util.MavenJarUtils;

/**
 * @author congbai
 * @date 2019/5/15
 */
public class SubmitJarHandler implements Handler<JarSubmitFlinkRequest, SubmitFlinkResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmitJarHandler.class);

    private final ClusterClient clusterClient;

    public SubmitJarHandler(ClusterClient clusterClient) {
        this.clusterClient = clusterClient;
    }

    @Override
    public SubmitFlinkResponse handle(JarSubmitFlinkRequest request) throws Exception {
        if (request.isTest()) {
            throw new UnsupportedOperationException();
        }
        LOGGER.trace("start submit jar request,entryClass:{}", request.getJarInfoDescriptor().getEntryClass());
        try {
            File file = MavenJarUtils.forAvg(request.getJarInfoDescriptor().getAvg()).getJarFile();
            List<String> programArgs = JarArgUtils.tokenizeArguments(request.getJarInfoDescriptor().getProgramArgs());
            PackagedProgram program = new PackagedProgram(file, request.getJarInfoDescriptor().getEntryClass(),
                programArgs.toArray(new String[programArgs.size()]));
            ClassLoader classLoader = null;
            try {
                classLoader = program.getUserCodeClassLoader();
            } catch (Exception e) {
                LOGGER.warn(e.getMessage());
            }

            Optimizer optimizer = new Optimizer(new DataStatistics(), new DefaultCostEstimator(), new Configuration());
            FlinkPlan plan
                = ClusterClient.getOptimizedPlan(optimizer, program, request.getJarInfoDescriptor().getParallelism());
            // set up the execution environment
            List<URL> jarFiles = FileUtils.createPath(file);
            JobSubmissionResult submissionResult
                = clusterClient.run(plan, jarFiles, Collections.emptyList(), classLoader);
            LOGGER.trace(" submit jar request sucess,jobId:{}", submissionResult.getJobID());
            return new SubmitFlinkResponse(true, submissionResult.getJobID().toString());
        } catch (Exception e) {
            String term = e.getMessage() == null ? "." : (": " + e.getMessage());
            LOGGER.error(" submit jar request fail", e);
            return new SubmitFlinkResponse(term);
        }
    }
}
