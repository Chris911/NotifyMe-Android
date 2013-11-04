package com.NotifyMe.Utilities;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class APIResponse {
    public JSONObject jsonResponse;
    public int status;
    public String error;
    public Boolean APISuccess = true;
    public String APIError = "";

    public APIResponse(HttpResponse response) {
        this.jsonResponse = responseToJSON(response);
        this.status = response.getStatusLine().getStatusCode();
        if(this.jsonResponse == null) {
            this.error = "Unable to get JSON response";
        } else {
            this.error = response.getStatusLine().getReasonPhrase();
            try {
                if(this.jsonResponse.getString("success").equals("false")) {
                    this.APISuccess = false;
                    this.APIError = this.jsonResponse.getString("error");
                }
            } catch (JSONException e) {

            }
        }
    }

    private JSONObject responseToJSON(HttpResponse response) {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));
            StringBuilder output = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                output.append(line);
            }
            return new JSONObject(output.toString());
        } catch (Exception e) {

        }
        return null;
    }
}
