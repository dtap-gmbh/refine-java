package gmbh.dtap.refine.client;

import gmbh.dtap.refine.api.RefineException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * This class implements a {@link ResponseHandler} for the <tt>apply operations</tt> response.
 *
 * @since 0.1.4
 */
class ApplyOperationsResponseHandler implements ResponseHandler<ApplyOperationsResponse> {

   private ResponseParser responseParser;

   ApplyOperationsResponseHandler(ResponseParser responseParser) {
      this.responseParser = responseParser;
   }

   /**
    * Validates the response and extracts necessary data.
    *
    * @param response the response to extract data from
    * @return the response representation
    * @throws IOException     in case of a connection problem
    * @throws RefineException in case the server responses with an error or is not understood
    * @since 0.1.4
    */
   @Override
   public ApplyOperationsResponse handleResponse(HttpResponse response) throws IOException {
      checkStatusCode(response);
      String responseBody = EntityUtils.toString(response.getEntity());
      return responseParser.parseApplyOperationsResponse(responseBody);
   }

   private void checkStatusCode(HttpResponse response) throws IOException {
      int status = response.getStatusLine().getStatusCode();
      if (status != 200) {
         throw new RefineException("Unexpected response status: " + status);
      }
   }
}