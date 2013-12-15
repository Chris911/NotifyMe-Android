package com.NotifyMe.Utilities;


import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class JSONUtilities {

    public static ArrayList<String> jsonArrayToList(JSONArray jsonArray) throws JSONException {
        ArrayList<String> list = new ArrayList<String>();
        if (jsonArray != null) {
            int len = jsonArray.length();
            for (int i=0;i<len;i++){
                list.add(jsonArray.get(i).toString());
            }
        }
        return list;
    }
}
