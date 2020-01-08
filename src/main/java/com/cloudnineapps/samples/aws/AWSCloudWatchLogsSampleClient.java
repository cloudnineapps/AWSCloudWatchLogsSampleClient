package com.cloudnineapps.samples.aws;

/**
 * Copyright © 2020 Cloud Nine Apps, LLC.
 * 
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.logs.AWSLogsClient;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.CreateLogGroupRequest;
import com.amazonaws.services.logs.model.CreateLogStreamRequest;
import com.amazonaws.services.logs.model.DescribeLogGroupsRequest;
import com.amazonaws.services.logs.model.DescribeLogGroupsResult;
import com.amazonaws.services.logs.model.DescribeLogStreamsRequest;
import com.amazonaws.services.logs.model.DescribeLogStreamsResult;
import com.amazonaws.services.logs.model.InputLogEvent;
import com.amazonaws.services.logs.model.PutLogEventsRequest;
import com.amazonaws.services.logs.model.PutRetentionPolicyRequest;

/**
 * Sample client for AWS CloudWatch Logs API.
 */
public class AWSCloudWatchLogsSampleClient {

	/** The log group name. */
	private static final String LOG_GROUP = "/myapp/onprem/component-1";

	/** The log stream name. */
	private static final String LOG_STREAM = "app-log";
	
	/** The log retention period (in days). */
	private static final int LOG_RETENTION_PERIOD = 1;

	/** The AWS region. */
	private static String Region = "us-east-1";
	
	/** The CloudWatch client. */
	private static AWSLogsClient Client;
	
	
	/** Opens the CloudWatch log. */
	public static void openCloudWatchLog() throws Exception {
		AWSCredentialsProvider creds = new DefaultAWSCredentialsProviderChain();
		Client = (AWSLogsClient) AWSLogsClientBuilder.standard()
				     .withCredentials(creds)
				     .withRegion(Region)
				     .build();
		// Create and set up the log group if it doesn't exist
		DescribeLogGroupsRequest request = new DescribeLogGroupsRequest().withLogGroupNamePrefix(LOG_GROUP);
		DescribeLogGroupsResult result = Client.describeLogGroups(request);
		if (result.getLogGroups().isEmpty()) {
			CreateLogGroupRequest logGroupRequest = new CreateLogGroupRequest(LOG_GROUP);
			Client.createLogGroup(logGroupRequest);
			PutRetentionPolicyRequest policyRequest = new PutRetentionPolicyRequest(LOG_GROUP, LOG_RETENTION_PERIOD);
			Client.putRetentionPolicy(policyRequest);
			CreateLogStreamRequest logStreamRequest = new CreateLogStreamRequest(LOG_GROUP, LOG_STREAM);
			Client.createLogStream(logStreamRequest);
			log("Created the log group and the log stream.");
		}
	}
	
	/** Logs the specified message. */
	public static void log(String msg) throws Exception {
		// Retrieve the sequence token in the log stream
		DescribeLogStreamsRequest request = new DescribeLogStreamsRequest().withLogGroupName(LOG_GROUP).withLogStreamNamePrefix(LOG_STREAM);
		DescribeLogStreamsResult result = Client.describeLogStreams(request);
		String seqToken = result.getLogStreams().get(0).getUploadSequenceToken();

		// Write to the log stream
		List<InputLogEvent> logEvents = new ArrayList<InputLogEvent>();
		InputLogEvent logEvent = new InputLogEvent().withMessage(msg).withTimestamp(System.currentTimeMillis());
		logEvents.add(logEvent);
		PutLogEventsRequest logRequest = new PutLogEventsRequest(LOG_GROUP, LOG_STREAM, logEvents).withSequenceToken(seqToken);
		Client.putLogEvents(logRequest);
	}
		
	/** Main */
	public static void main(String[] args) throws Exception {
		System.out.println("Launching the application...");
		openCloudWatchLog();
		// Sample log statements
		log("Starting the app...");
		log("Another message");
		System.out.println("Execution completed.");
	}
}