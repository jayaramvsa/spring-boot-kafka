package io.jay.kafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jay.kafka.domain.Book;
import io.jay.kafka.domain.LibraryEvent;
import io.jay.kafka.producer.LibraryEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventsController.class)
@AutoConfigureMockMvc
public class LibraryEventsControllerUnitTest {

    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper= new ObjectMapper();

    @MockBean
    LibraryEventProducer libraryEventProducer;

    @Test
    void postLibraryEvent() throws Exception {
        //given
        Book book = Book.builder().bookId(1).bookAuthor("Hamang Lee").bookName("How To Train a Dragon").build();
        LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(1).book(book).build();
        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventProducer.sendLibraryEventUsingProduceRecord(isA(LibraryEvent.class))).thenReturn(null);

        //when
        mockMvc.perform(post("/v1/libraryEvent").content(json).
                contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
    }

    @Test
    void postLibraryEventIs4xx() throws Exception {
        //given
        Book book = Book.builder().bookId(null).bookAuthor(null).bookName("How to train your Dragon").build();
        LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(null).book(book).build();
        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventProducer.sendLibraryEventUsingProduceRecord(isA(LibraryEvent.class))).thenReturn(null);

        //expect
        String errorMessage = "book.bookAuthor-must not be blank, book.bookId-must not be null";
        mockMvc.perform(post("/v1/pr/libraryEvent").content(json).
                contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError())
        .andExpect(content().string(errorMessage));
    }
}
