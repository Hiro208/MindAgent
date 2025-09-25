package com.mind.agent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import org.springframework.jdbc.core.ConnectionCallback;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DataBaseTools implements Tool {

    private final JdbcTemplate jdbcTemplate;

    public DataBaseTools(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getName() {
        return "dataBaseTool";
    }

    @Override
    public String getDescription() {
        return "Reads data from PostgreSQL using database queries.";
    }

    @Override
    public ToolType getType() {
        return ToolType.OPTIONAL;
    }

    @org.springframework.ai.tool.annotation.Tool(name = "databaseQuery", description = "Execute read-only SELECT queries against PostgreSQL and return structured results. Writes and database modifications are prohibited.")
    public String query(String sql) {
        try {

            String trimmedSql = sql == null ? "" : sql.trim().toUpperCase(java.util.Locale.ROOT);
            if (!trimmedSql.matches("(?s)SELECT\\b.*") || sql.contains(";")) {
                log.warn("Rejected non-SELECT query: {}", sql);
                return "Error：Only SELECT queries are supported. Provided SQL: " + sql;
            }

            List<String> rows = jdbcTemplate.execute((ConnectionCallback<List<String>>) connection -> {
                boolean originalAutoCommit = connection.getAutoCommit();
                if (!originalAutoCommit) {
                    throw new java.sql.SQLException("Query tool requires an independent connection");
                }
                connection.setAutoCommit(false);
                try (var setup = connection.createStatement()) {
                    setup.execute("SET TRANSACTION READ ONLY");
                    setup.execute("SET LOCAL statement_timeout = '5s'");
                    try (var statement = connection.createStatement()) {
                        statement.setMaxRows(100);
                        statement.setQueryTimeout(5);
                        try (ResultSet rs = statement.executeQuery(sql)) {
                            List<String> resultRows = new ArrayList<>();
                            ResultSetMetaData metaData = rs.getMetaData();
                            int columnCount = metaData.getColumnCount();

                            if (columnCount == 0) {
                                resultRows.add("The query returned no columns");
                                return resultRows;
                            }

                            List<String> columnNames = new ArrayList<>();
                            List<Integer> columnWidths = new ArrayList<>();
                            for (int i = 1; i <= columnCount; i++) {
                                String columnName = metaData.getColumnName(i);
                                columnNames.add(columnName);
                                columnWidths.add(columnName.length());
                            }

                            List<List<String>> dataRows = new ArrayList<>();
                            while (rs.next()) {
                                List<String> rowData = new ArrayList<>();
                                for (int i = 1; i <= columnCount; i++) {
                                    Object value = rs.getObject(i);
                                    String valueStr = value == null ? "NULL" : value.toString();
                                    rowData.add(valueStr);

                                    int currentWidth = columnWidths.get(i - 1);
                                    if (valueStr.length() > currentWidth) {
                                        columnWidths.set(i - 1, valueStr.length());
                                    }
                                }
                                dataRows.add(rowData);
                            }

                            StringBuilder header = new StringBuilder();
                            header.append("| ");
                            for (int i = 0; i < columnCount; i++) {
                                String columnName = columnNames.get(i);
                                int width = columnWidths.get(i);
                                header.append(String.format("%-" + width + "s", columnName)).append(" | ");
                            }
                            resultRows.add(header.toString());

                            StringBuilder separator = new StringBuilder();
                            separator.append("|");
                            for (int i = 0; i < columnCount; i++) {
                                int width = columnWidths.get(i);
                                separator.append("-".repeat(width + 2)).append("|");
                            }
                            resultRows.add(separator.toString());

                            if (dataRows.isEmpty()) {
                                StringBuilder emptyRow = new StringBuilder();
                                emptyRow.append("| ");
                                int totalWidth = columnWidths.stream().mapToInt(w -> w + 3).sum() - 1;
                                emptyRow.append(String.format("%-" + (totalWidth - 2) + "s", "(No data)"));
                                emptyRow.append(" |");
                                resultRows.add(emptyRow.toString());
                            } else {
                                for (List<String> rowData : dataRows) {
                                    StringBuilder row = new StringBuilder();
                                    row.append("| ");
                                    for (int i = 0; i < columnCount; i++) {
                                        String value = rowData.get(i);
                                        int width = columnWidths.get(i);
                                        row.append(String.format("%-" + width + "s", value)).append(" | ");
                                    }
                                    resultRows.add(row.toString());
                                }
                            }

                            return resultRows;
                        }
                    }
                } finally {
                    try {
                        connection.rollback();
                    } finally {
                        connection.setAutoCommit(originalAutoCommit);
                    }
                }
            });

            int dataRowCount = rows.size() - 2;
            if (rows.size() > 2 && rows.get(rows.size() - 1).contains("(No data)")) {
                dataRowCount = 0;
            }

            log.info("SQL query completed; rows returned: {} rows", dataRowCount);

            return "Query results:\n" + String.join("\n", rows);
        } catch (Exception e) {
            log.error("UnknownError: {}", e.getMessage(), e);
            return "Error：Operation failed - " + e.getMessage() + "\nSQL: " + sql;
        }
    }
}
