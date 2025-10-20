package com.mind;

import com.mind.agent.tools.DataBaseTools;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataBaseToolsTest {
    @Test
    void rejectsInvalidQueriesWithoutAccessingDatabase() {
        var jdbc = mock(JdbcTemplate.class);
        var tool = new DataBaseTools(jdbc);
        for (String sql : new String[] {null, "", "DELETE FROM agent", "SELECT 1; DELETE FROM agent", "SELECTED"}) {
            assertTrue(tool.query(sql).startsWith("Error：Only supports"));
        }
        verifyNoInteractions(jdbc);
    }
}
