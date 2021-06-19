package io.jay.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jay.kafka.domain.Book;
import io.jay.kafka.domain.LibraryEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
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

    @Test
    void sendLibraryEventUsingProduceRecord_success() throws JsonProcessingException, ExecutionException, InterruptedException {

        //given
        Book book = Book.builder().bookId(1).bookAuthor("Hamang Lee").bookName("How To Train a Dragon").build();
        LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(1).book(book).build();
        String message = objectMapper.writeValueAsString(libraryEvent);
        SettableListenableFuture settableListenableFuture = new SettableListenableFuture();
        ProducerRecord<Integer, String> producerRecord = new ProducerRecord("library-events",libraryEvent.getLibraryEventId(),message);
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("library-events",1),1,1,123,System.currentTimeMillis(),1,2);
        SendResult<Integer,String> sendResult = new SendResult<Integer,String>(producerRecord,recordMetadata);
        settableListenableFuture.set(sendResult);

        //when
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(settableListenableFuture);
        ListenableFuture<SendResult<Integer,String>> listenableFuture = libraryEventProducer.sendLibraryEventUsingProduceRecord(libraryEvent);

        //expect
        SendResult<Integer,String> sendResult1 = listenableFuture.get();
        assert sendResult1.getRecordMetadata().partition() == 1;
    }
}
