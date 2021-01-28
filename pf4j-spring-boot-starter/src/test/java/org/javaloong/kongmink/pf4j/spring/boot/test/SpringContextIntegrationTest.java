package org.javaloong.kongmink.pf4j.spring.boot.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.pf4j.PluginManager;
import org.pf4j.update.UpdateManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class SpringContextIntegrationTest {

    @Autowired ApplicationContext ctx;
    
    @Test
    public void contextLoads(){
        assertNotNull(ctx);
        assertNotNull(ctx.getBean(PluginManager.class));
        assertNotNull(ctx.getBean(UpdateManager.class));
    }
}
