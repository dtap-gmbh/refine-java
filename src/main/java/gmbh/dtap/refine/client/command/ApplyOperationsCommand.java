/*
 * Copyright 2019 DTAP GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gmbh.dtap.refine.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import gmbh.dtap.refine.client.*;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static gmbh.dtap.refine.client.util.HttpParser.HTTP_PARSER;
import static gmbh.dtap.refine.client.util.JsonParser.JSON_PARSER;
import static org.apache.commons.lang3.Validate.*;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

/**
 * A command to apply operations on a project.
 */
public class ApplyOperationsCommand implements ResponseHandler<ApplyOperationsResponse> {

	private final String projectId;
	private final Operation[] operations;
	private String token;
	private final String CSRF_TOKEN = "csrf_token=";

	/**
	 * Constructor for {@link Builder}.
	 *
	 * @param projectId  the project ID
	 * @param operations the operations
	 */
	private ApplyOperationsCommand(String projectId, Operation[] operations, String token) {
		this.projectId = projectId;
		this.operations = operations;
		this.token = token;
	}

	/**
	 * Executes the command.
	 *
	 * @param client the client to execute the command with
	 * @return the result of the command
	 * @throws IOException     in case of a connection problem
	 * @throws RefineException in case the server responses with an error or is not
	 *                         understood
	 */
	public ApplyOperationsResponse execute(RefineClient client) throws IOException {
		URL url = client.createUrl("/command/core/apply-operations?" + CSRF_TOKEN + token);

		List<NameValuePair> form = new ArrayList<>();
		form.add(new BasicNameValuePair("project", projectId));
		StringJoiner operationsJoiner = new StringJoiner(",");
		for (Operation operation : operations) {
			operationsJoiner.add(operation.asJson());
		}
		String operationsJsonArray = "[" + operationsJoiner.toString() + "]";
		form.add(new BasicNameValuePair("operations", operationsJsonArray));

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);

		HttpUriRequest request = RequestBuilder.post(url.toString()).setHeader(ACCEPT, APPLICATION_JSON.getMimeType())
				.setEntity(entity).build();

		return client.execute(request, this);
	}

	/**
	 * Validates the response and extracts necessary data.
	 *
	 * @param response the response to extract data from
	 * @return the response representation
	 * @throws IOException     in case of a connection problem
	 * @throws RefineException in case the server responses with an unexpected
	 *                         status or is not understood
	 */
	@Override
	public ApplyOperationsResponse handleResponse(HttpResponse response) throws IOException {
		HTTP_PARSER.assureStatusCode(response, SC_OK);
		String responseBody = EntityUtils.toString(response.getEntity());
		return parseApplyOperationsResponse(responseBody);
	}

	ApplyOperationsResponse parseApplyOperationsResponse(String json) throws IOException {
		JsonNode node = JSON_PARSER.parseJson(json);
		String code = JSON_PARSER.findExistingPath(node, "code").asText();
		if ("ok".equals(code)) {
			return ApplyOperationsResponse.ok();
		} else if ("pending".equals(code)) {
			return ApplyOperationsResponse.pending();
		} else if ("error".equals(code)) {
			String message = JSON_PARSER.findExistingPath(node, "message").asText();
			return ApplyOperationsResponse.error(message);
		} else {
			throw new RefineException("Unexpected code: " + code);
		}
	}

	/**
	 * The builder for {@link ApplyOperationsCommand}.
	 */
	public static class Builder {

		private String projectId;
		private Operation[] operations;
		private String token;

		/**
		 * Sets the project ID.
		 *
		 * @param projectId the project ID
		 * @return the builder for fluent usage
		 */
		public Builder project(String projectId) {
			this.projectId = projectId;
			return this;
		}

		/**
		 * Sets token.
		 *
		 * @param token the csrf token
		 * @return the builder for fluent usage
		 */
		public Builder token(String token) {
			this.token = token;
			return this;
		}

		/**
		 * Sets the project ID from the project location.
		 *
		 * @param projectLocation the project location
		 * @return the builder for fluent usage
		 */
		public Builder project(ProjectLocation projectLocation) {
			notNull(projectLocation, "projectLocation");
			this.projectId = projectLocation.getId();
			return this;
		}

		/**
		 * Sets the project ID from the project.
		 *
		 * @param project the project
		 * @return the builder for fluent usage
		 */
		public Builder project(RefineProject project) {
			notNull(project, "project");
			this.projectId = project.getId();
			return this;
		}

		/**
		 * Sets one or more operations.
		 *
		 * @param operations the operations
		 * @return the builder for fluent usage
		 */
		public Builder operations(Operation... operations) {
			this.operations = operations;
			return this;
		}

		/**
		 * Builds the command after validation.
		 *
		 * @return the command
		 */
		public ApplyOperationsCommand build() {
			notNull(projectId, "projectId");
			notEmpty(projectId, "projectId is empty");
			notNull(operations, "operations");
			notEmpty(operations, "operations is empty");
			notEmpty(token, "token");
			noNullElements(operations, "operations contains null");
			return new ApplyOperationsCommand(projectId, operations, token);
		}
	}
}
