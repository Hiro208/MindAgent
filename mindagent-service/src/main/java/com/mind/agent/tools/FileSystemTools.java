package com.mind.agent.tools;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class FileSystemTools implements Tool {

    private static final String BASE_DIRECTORY = System.getProperty("user.dir");

    @Override
    public String getName() {
        return "fileSystemTool";
    }

    @Override
    public String getDescription() {
        return "Provides file operations including reading, writing, and directory listing.";
    }

    @Override
    public ToolType getType() {
        return ToolType.OPTIONAL;
    }

    @org.springframework.ai.tool.annotation.Tool(
            name = "readFile",
            description = "Read a file. filePath is relative to the working directory."
    )
    public String readFile(String filePath) {
        try {
            Path path = validateAndResolvePath(filePath);

            if (!Files.exists(path)) {
                return "Error：File does not exist - " + filePath;
            }

            if (!Files.isRegularFile(path)) {
                return "Error：Path is not a file - " + filePath;
            }

            String content = Files.readString(path);
            log.info("File read: {}", filePath);
            return "File contents:\n" + content;

        } catch (SecurityException e) {
            log.error("Security error: {}", e.getMessage());
            return "Error：Access denied - " + e.getMessage();
        } catch (IOException e) {
            log.error("Failed to read file: {}", filePath, e);
            return "Error：Failed to read file - " + e.getMessage();
        } catch (Exception e) {
            log.error("UnknownError: {}", e.getMessage(), e);
            return "Error：Operation failed - " + e.getMessage();
        }
    }

    @org.springframework.ai.tool.annotation.Tool(
            name = "writeFile",
            description = "Write content to a file, creating it if missing or overwriting it if present. Parameters: filePath (relative to the working directory), content (text to write)."
    )
    public String writeFile(String filePath, String content) {
        try {
            Path path = validateAndResolvePath(filePath);

            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
                log.info("Create directory: {}", parent);
            }

            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            log.info("File written: {}", filePath);
            return "File written: " + filePath;

        } catch (SecurityException e) {
            log.error("Security error: {}", e.getMessage());
            return "Error：Access denied - " + e.getMessage();
        } catch (IOException e) {
            log.error("Failed to write file: {}", filePath, e);
            return "Error：Failed to write file - " + e.getMessage();
        } catch (Exception e) {
            log.error("UnknownError: {}", e.getMessage(), e);
            return "Error：Operation failed - " + e.getMessage();
        }
    }

    @org.springframework.ai.tool.annotation.Tool(
            name = "appendToFile",
            description = "Append content to a file, creating it if missing. Parameters: filePath (relative to the working directory), content (text to append)."
    )
    public String appendToFile(String filePath, String content) {
        try {
            Path path = validateAndResolvePath(filePath);

            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
                log.info("Create directory: {}", parent);
            }

            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            log.info("Content appended to file: {}", filePath);
            return "Content appended to file: " + filePath;

        } catch (SecurityException e) {
            log.error("Security error: {}", e.getMessage());
            return "Error：Access denied - " + e.getMessage();
        } catch (IOException e) {
            log.error("Failed to append file content: {}", filePath, e);
            return "Error：Failed to append content - " + e.getMessage();
        } catch (Exception e) {
            log.error("UnknownError: {}", e.getMessage(), e);
            return "Error：Operation failed - " + e.getMessage();
        }
    }

    @org.springframework.ai.tool.annotation.Tool(
            name = "listFiles",
            description = "List files and subdirectories. directoryPath is relative to the working directory; leave it empty to list the current directory."
    )
    public String listFiles(String directoryPath) {
        try {
            Path path;
            if (directoryPath == null || directoryPath.trim().isEmpty()) {
                path = Paths.get(BASE_DIRECTORY);
            } else {
                path = validateAndResolvePath(directoryPath);
            }

            if (!Files.exists(path)) {
                return "Error：Directory does not exist - " + directoryPath;
            }

            if (!Files.isDirectory(path)) {
                return "Error：Path is not a directory - " + directoryPath;
            }

            List<String> items = Files.list(path)
                    .map(p -> {
                        String name = p.getFileName().toString();
                        if (Files.isDirectory(p)) {
                            return "[DIR] " + name;
                        } else {
                            try {
                                long size = Files.size(p);
                                return "[FILE] " + name + " (" + formatFileSize(size) + ")";
                            } catch (IOException e) {
                                return "[FILE] " + name;
                            }
                        }
                    })
                    .sorted()
                    .collect(Collectors.toList());

            if (items.isEmpty()) {
                return "Directory is empty: " + directoryPath;
            }

            log.info("Directory listed: {}", directoryPath);
            return "Directory contents (" + directoryPath + "):\n" + String.join("\n", items);

        } catch (SecurityException e) {
            log.error("Security error: {}", e.getMessage());
            return "Error：Access denied - " + e.getMessage();
        } catch (IOException e) {
            log.error("Failed to list directory: {}", directoryPath, e);
            return "Error：Failed to list directory - " + e.getMessage();
        } catch (Exception e) {
            log.error("UnknownError: {}", e.getMessage(), e);
            return "Error：Operation failed - " + e.getMessage();
        }
    }

    @org.springframework.ai.tool.annotation.Tool(
            name = "deleteFile",
            description = "Delete a file or directory. path is relative to the working directory."
    )
    public String deleteFile(String path) {
        try {
            Path filePath = validateAndResolvePath(path);

            if (!Files.exists(filePath)) {
                return "Error：File or directory does not exist - " + path;
            }

            if (Files.isDirectory(filePath)) {

                try (Stream<Path> paths = Files.walk(filePath)) {
                    paths.sorted((a, b) -> b.compareTo(a))
                            .forEach(p -> {
                                try {
                                    Files.delete(p);
                                } catch (IOException e) {
                                    log.warn("Deletion failed: {}", p, e);
                                }
                            });
                }
                log.info("Directory deleted: {}", path);
                return "Directory deleted: " + path;
            } else {
                Files.delete(filePath);
                log.info("File deleted: {}", path);
                return "File deleted: " + path;
            }

        } catch (SecurityException e) {
            log.error("Security error: {}", e.getMessage());
            return "Error：Access denied - " + e.getMessage();
        } catch (IOException e) {
            log.error("Failed to delete file: {}", path, e);
            return "Error：Deletion failed - " + e.getMessage();
        } catch (Exception e) {
            log.error("UnknownError: {}", e.getMessage(), e);
            return "Error：Operation failed - " + e.getMessage();
        }
    }

    @org.springframework.ai.tool.annotation.Tool(
            name = "createDirectory",
            description = "Create a directory and any missing parents. directoryPath is relative to the working directory."
    )
    public String createDirectory(String directoryPath) {
        try {
            Path path = validateAndResolvePath(directoryPath);

            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    return "Directory already exists: " + directoryPath;
                } else {
                    return "Error：Path exists but is not a directory - " + directoryPath;
                }
            }

            Files.createDirectories(path);
            log.info("Directory created: {}", directoryPath);
            return "Directory created: " + directoryPath;

        } catch (SecurityException e) {
            log.error("Security error: {}", e.getMessage());
            return "Error：Access denied - " + e.getMessage();
        } catch (IOException e) {
            log.error("Failed to create directory: {}", directoryPath, e);
            return "Error：Failed to create directory - " + e.getMessage();
        } catch (Exception e) {
            log.error("UnknownError: {}", e.getMessage(), e);
            return "Error：Operation failed - " + e.getMessage();
        }
    }

    private Path validateAndResolvePath(String filePath) throws SecurityException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path is required");
        }

        Path basePath = Paths.get(BASE_DIRECTORY).toAbsolutePath().normalize();
        Path resolvedPath = basePath.resolve(filePath).toAbsolutePath().normalize();

        if (!resolvedPath.startsWith(basePath)) {
            throw new SecurityException("Path traversal blocked: " + filePath);
        }

        return resolvedPath;
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
