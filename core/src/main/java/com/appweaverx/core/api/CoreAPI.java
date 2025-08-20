package com.appweaverx.core.api;

import com.appweaverx.core.api.model.CoreInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/core")
public class CoreAPI {

    @GetMapping()
    public ResponseEntity<CoreInfo> getCoreInfo() {
        CoreInfo coreInfo = new CoreInfo();
        coreInfo.setMessage("Welcome to AppWeaverX Core API");
        return ResponseEntity.ok(coreInfo);
    }
}
