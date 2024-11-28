package org.rdx.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ author rdx
 * @ describe :
 * @ date  2024/11/22
 **/
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {
    @Test
    public void test(){
        System.out.println("aaa1");
        System.out.println("aaa2");
        System.out.println("aaa3");
        System.out.println("aaa4");
    }

}
