package com.agenda.agendaespacios.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/debug")
public class DebugController {

    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);

    @GetMapping("")
    public String debugPage() {
        return "debug";
    }

    @GetMapping("/ping")
    @ResponseBody
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok("Service is running");
    }

    @PostMapping("/files")
    @ResponseBody
    public ResponseEntity<?> debugFiles(
            @RequestParam("configFile") MultipartFile configFile,
            @RequestParam("peticionesFile") MultipartFile peticionesFile) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Debug config file
            Map<String, Object> configInfo = new HashMap<>();
            configInfo.put("name", configFile.getOriginalFilename());
            configInfo.put("size", configFile.getSize());
            configInfo.put("contentType", configFile.getContentType());
            
            // Read the content
            String configContent = new BufferedReader(
                new InputStreamReader(configFile.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            
            configInfo.put("content", configContent);
            configInfo.put("lineCount", configContent.split("\n").length);
            
            // Check for BOM or other unusual characters
            StringBuilder hexContent = new StringBuilder();
            for (byte b : configContent.getBytes(StandardCharsets.UTF_8)) {
                hexContent.append(String.format("%02X ", b));
            }
            configInfo.put("hexContent", hexContent.toString());
            
            result.put("configFile", configInfo);
            
            // Debug peticiones file
            Map<String, Object> peticionesInfo = new HashMap<>();
            peticionesInfo.put("name", peticionesFile.getOriginalFilename());
            peticionesInfo.put("size", peticionesFile.getSize());
            peticionesInfo.put("contentType", peticionesFile.getContentType());
            
            // Read first few lines
            String peticionesContent = new BufferedReader(
                new InputStreamReader(peticionesFile.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .limit(5)  // Just show the first 5 lines
                    .collect(Collectors.joining("\n"));
            
            peticionesInfo.put("sampleContent", peticionesContent);
            
            result.put("peticionesFile", peticionesInfo);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error in debug endpoint", e);
            result.put("error", e.getMessage());
            result.put("stackTrace", e.getStackTrace());
            return ResponseEntity.status(500).body(result);
        }
    }
} 