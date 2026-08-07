package edu.uc.eh.controller;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reports which build is running, so a deployed server can be matched to a
 * commit and to a published container image tag.
 */
@RestController
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.OPTIONS}
)
public class VersionController {

    private static final String UNKNOWN = "unknown";

    // Absent when build-info.properties was not generated, e.g. running from an IDE.
    private final BuildProperties buildProperties;

    public VersionController(ObjectProvider<BuildProperties> buildProperties) {
        this.buildProperties = buildProperties.getIfAvailable();
    }

    @RequestMapping(value = "api/version", method = RequestMethod.GET)
    @ResponseBody
    public JSONObject version() {
        JSONObject json = new JSONObject();

        if (buildProperties == null) {
            json.put("version", UNKNOWN);
            json.put("commit", UNKNOWN);
            json.put("buildTime", UNKNOWN);
        } else {
            json.put("version", orUnknown(buildProperties.getVersion()));
            json.put("commit", orUnknown(buildProperties.get("commit")));
            json.put("buildTime", buildProperties.getTime() == null
                    ? UNKNOWN
                    : buildProperties.getTime().toString());
        }

        json.put("java", System.getProperty("java.version"));
        return json;
    }

    private static String orUnknown(String value) {
        return value == null || value.isEmpty() ? UNKNOWN : value;
    }
}
