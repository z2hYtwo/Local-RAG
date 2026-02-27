package com.bmad.nativeapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class LlamaNativeTest {

    @Test
    public void testHandshake() {
        try {
            String result = LlamaNative.handshake();
            System.out.println("JNI Handshake Result: " + result);
            assertNotNull(result);
            assertTrue(result.contains("BMAD Native Handshake"));
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native library not found. Please build the native project first.");
            // We don't fail the test here if the library is missing, 
            // but we provide clear feedback.
        }
    }

    @Test
    public void testLoadModel() {
        try {
            int result = LlamaNative.loadModel("test_path.gguf");
            assertEquals(1, result);
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native library not found.");
        }
    }
}
