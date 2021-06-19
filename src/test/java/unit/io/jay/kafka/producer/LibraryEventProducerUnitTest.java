package io.jay.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jay.kafka.domain.Book;
import io.jay.kafka.domain.LibraryEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryEventProducerUnitTest {

    @Mock
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    LibraryEventProducer libraryEventProducer;


    @Test
    void sendLibraryEventUsingProduceRecord_failure() throws JsonProcessingException, ExecutionException, InterruptedException {

        //given
        Book book = Book.builder().bookId(1).bookAuthor("Hamang Lee").bookName("How To Train a Dragon").build();
        LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(1).book(book).build();
        SettableListenableFuture settableListenableFuture = new SettableListenableFuture();
        settableListenableFuture.setException(new RuntimeException("Exception Calling kafka"));

        //when
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(settableListenableFuture);

        //expect
        assertThrows(Exception.class, () -> libraryEventProducer.sendLibraryEventUsingProduceRecord(libraryEvent).get());
    }
}
