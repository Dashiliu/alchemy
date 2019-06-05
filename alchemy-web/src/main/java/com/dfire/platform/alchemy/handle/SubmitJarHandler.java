package com.dfire.platform.alchemy.handle;

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
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;
import org.apache.flink.table.shaded.org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dfire.platform.alchemy.client.FlinkClient;
import com.dfire.platform.alchemy.handle.request.JarSubmitFlinkRequest;
import com.dfire.platform.alchemy.handle.response.SubmitFlinkResponse;
import com.dfire.platform.alchemy.util.FileUtil;
import com.dfire.platform.alchemy.util.JarArgUtil;
import com.dfire.platform.alchemy.util.MavenJarUtil;

/**
 * @author congbai
 * @date 2019/5/15
 */
@Component
public class SubmitJarHandler implements Handler<JarSubmitFlinkRequest, SubmitFlinkResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmitJarHandler.class);

    @Override
    public SubmitFlinkResponse handle(FlinkClient client, JarSubmitFlinkRequest request) throws Exception {
        if (request.isTest()) {
            throw new UnsupportedOperationException();
        }
        LOGGER.trace("start submit jar request,entryClass:{}", request.getEntryClass());
        try {
            File file = MavenJarUtil.forAvg(request.getAvg()).getJarFile();
            List<String> programArgs = JarArgUtil.tokenizeArguments(request.getProgramArgs());
            PackagedProgram program = new PackagedProgram(file, request.getEntryClass(),
                programArgs.toArray(new String[programArgs.size()]));
            ClassLoader classLoader = null;
            try {
                classLoader = program.getUserCodeClassLoader();
            } catch (Exception e) {
                LOGGER.warn(e.getMessage());
            }

            Optimizer optimizer = new Optimizer(new DataStatistics(), new DefaultCostEstimator(), new Configuration());
            FlinkPlan plan = ClusterClient.getOptimizedPlan(optimizer, program, request.getParallelism());
            // Savepoint restore settings
            SavepointRestoreSettings savepointSettings = SavepointRestoreSettings.none();
            String savepointPath = request.getSavepointPath();
            if (StringUtils.isNotEmpty(savepointPath)) {
                Boolean allowNonRestoredOpt = request.getAllowNonRestoredState();
                boolean allowNonRestoredState = allowNonRestoredOpt != null && allowNonRestoredOpt.booleanValue();
                savepointSettings = SavepointRestoreSettings.forPath(savepointPath, allowNonRestoredState);
            }
            // set up the execution environment
            List<URL> jarFiles = FileUtil.createPath(file);
            JobSubmissionResult submissionResult = client.getClusterClient().run(plan, jarFiles,
                Collections.emptyList(), classLoader, savepointSettings);
            LOGGER.trace(" submit jar request sucess,jobId:{}", submissionResult.getJobID());
            return new SubmitFlinkResponse(true, submissionResult.getJobID().toString());
        } catch (Exception e) {
            String term = e.getMessage() == null ? "." : (": " + e.getMessage());
            LOGGER.error(" submit jar request fail", e);
            return new SubmitFlinkResponse(term);
        }
    }
}
