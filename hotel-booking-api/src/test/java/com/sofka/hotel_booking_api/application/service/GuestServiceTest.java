package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.model.Guest;
import com.sofka.hotel_booking_api.domain.repository.GuestRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateGuestRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para GuestService.
 * Historia 3.2: Registrar información completa del huésped
 */
@ExtendWith(MockitoExtension.class)
class GuestServiceTest {

    @Mock
    private GuestRepository guestRepository;

    @InjectMocks
    private GuestService guestService;

    private CreateGuestRequest validGuestRequest;
    private Guest existingGuest;

    @BeforeEach
    void setUp() {
        validGuestRequest = new CreateGuestRequest(
                "John",
                "Doe",
                "12345678",
                "john.doe@example.com",
                "+1-555-1234"
        );

        existingGuest = new Guest(
                "Jane",
                "Smith",
                "12345678",
                "jane.smith@example.com",
                "+1-555-5678"
        );
    }

    // ============================================
    // Escenario: Registro de nuevo huésped
    // ============================================

    @Test
    @DisplayName("Debe registrar un nuevo huésped cuando no existe con ese documento")
    void shouldRegisterNewGuestWhenDocumentDoesNotExist() {
        // Given - No existe huésped con ese documento
        when(guestRepository.findByDocumentNumber(validGuestRequest.documentNumber()))
                .thenReturn(Optional.empty());
        
        Guest savedGuest = new Guest(
                validGuestRequest.firstName(),
                validGuestRequest.lastName(),
                validGuestRequest.documentNumber(),
                validGuestRequest.email(),
                validGuestRequest.phone()
        );
        when(guestRepository.save(any(Guest.class))).thenReturn(savedGuest);

        // When - Registro el huésped
        Guest result = guestService.registerOrUpdateGuest(validGuestRequest);

        // Then - Se crea un nuevo huésped
        assertNotNull(result);
        assertEquals(validGuestRequest.firstName(), result.getFirstName());
        assertEquals(validGuestRequest.lastName(), result.getLastName());
        assertEquals(validGuestRequest.documentNumber(), result.getDocumentNumber());
        assertEquals(validGuestRequest.email(), result.getEmail());
        assertEquals(validGuestRequest.phone(), result.getPhone());

        // Verificar que se busco por documento
        verify(guestRepository, times(1)).findByDocumentNumber(validGuestRequest.documentNumber());
        
        // Verificar que se guardo un nuevo huésped
        ArgumentCaptor<Guest> guestCaptor = ArgumentCaptor.forClass(Guest.class);
        verify(guestRepository, times(1)).save(guestCaptor.capture());
        
        Guest capturedGuest = guestCaptor.getValue();
        assertEquals(validGuestRequest.firstName(), capturedGuest.getFirstName());
        assertEquals(validGuestRequest.documentNumber(), capturedGuest.getDocumentNumber());
    }

    @Test
    @DisplayName("Debe crear un huésped con todos los campos del request")
    void shouldCreateGuestWithAllFieldsFromRequest() {
        // Given
        when(guestRepository.findByDocumentNumber(validGuestRequest.documentNumber()))
                .thenReturn(Optional.empty());
        
        Guest savedGuest = new Guest(
                validGuestRequest.firstName(),
                validGuestRequest.lastName(),
                validGuestRequest.documentNumber(),
                validGuestRequest.email(),
                validGuestRequest.phone()
        );
        when(guestRepository.save(any(Guest.class))).thenReturn(savedGuest);

        // When
        Guest result = guestService.registerOrUpdateGuest(validGuestRequest);

        // Then - Todos los campos fueron copiados correctamente
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("12345678", result.getDocumentNumber());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("+1-555-1234", result.getPhone());
    }

    // ============================================
    // Escenario: Actualización de huésped existente
    // ============================================

    @Test
    @DisplayName("Debe actualizar un huésped existente cuando el documento ya existe")
    void shouldUpdateExistingGuestWhenDocumentExists() {
        // Given - Ya existe un huésped con ese documento
        when(guestRepository.findByDocumentNumber(validGuestRequest.documentNumber()))
                .thenReturn(Optional.of(existingGuest));
        
        when(guestRepository.save(existingGuest)).thenReturn(existingGuest);

        // When - Intento registrar con el mismo documento
        Guest result = guestService.registerOrUpdateGuest(validGuestRequest);

        // Then - Se actualiza el huésped existente
        assertNotNull(result);
        assertEquals(validGuestRequest.firstName(), result.getFirstName());
        assertEquals(validGuestRequest.lastName(), result.getLastName());
        assertEquals(validGuestRequest.email(), result.getEmail());
        assertEquals(validGuestRequest.phone(), result.getPhone());
        assertEquals(validGuestRequest.documentNumber(), result.getDocumentNumber());

        // Verificar que se buscó por documento
        verify(guestRepository, times(1)).findByDocumentNumber(validGuestRequest.documentNumber());
        
        // Verificar que se guardó el huésped actualizado
        verify(guestRepository, times(1)).save(existingGuest);
    }

    @Test
    @DisplayName("Debe actualizar todos los campos del huésped existente excepto el ID")
    void shouldUpdateAllFieldsExceptId() {
        // Given - Huésped existente con datos diferentes
        Guest originalGuest = new Guest(
                "OldFirstName",
                "OldLastName",
                "12345678",
                "old@example.com",
                "+1-555-0000"
        );
        
        when(guestRepository.findByDocumentNumber(validGuestRequest.documentNumber()))
                .thenReturn(Optional.of(originalGuest));
        
        when(guestRepository.save(originalGuest)).thenReturn(originalGuest);

        // When - Actualizo con nuevos datos
        Guest result = guestService.registerOrUpdateGuest(validGuestRequest);

        // Then - Todos los campos fueron actualizados
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("+1-555-1234", result.getPhone());
        assertEquals("12345678", result.getDocumentNumber()); // Documento no cambia
    }

    @Test
    @DisplayName("No debe crear un nuevo huésped si el documento ya existe")
    void shouldNotCreateNewGuestWhenDocumentExists() {
        // Given
        when(guestRepository.findByDocumentNumber(validGuestRequest.documentNumber()))
                .thenReturn(Optional.of(existingGuest));
        
        when(guestRepository.save(existingGuest)).thenReturn(existingGuest);

        // When
        guestService.registerOrUpdateGuest(validGuestRequest);

        // Then - Solo se guarda una vez (el update), no se crea nuevo
        verify(guestRepository, times(1)).save(existingGuest);
        verify(guestRepository, never()).save(argThat(guest -> 
                guest != existingGuest && 
                guest.getDocumentNumber().equals(validGuestRequest.documentNumber())
        ));
    }

    // ============================================
    // Escenario: Verificación de transaccionalidad
    // ============================================

    @Test
    @DisplayName("Debe llamar al repositorio exactamente una vez para buscar por documento")
    void shouldCallRepositoryOnceToFindByDocument() {
        // Given
        when(guestRepository.findByDocumentNumber(validGuestRequest.documentNumber()))
                .thenReturn(Optional.empty());
        
        Guest savedGuest = new Guest(
                validGuestRequest.firstName(),
                validGuestRequest.lastName(),
                validGuestRequest.documentNumber(),
                validGuestRequest.email(),
                validGuestRequest.phone()
        );
        when(guestRepository.save(any(Guest.class))).thenReturn(savedGuest);

        // When
        guestService.registerOrUpdateGuest(validGuestRequest);

        // Then
        verify(guestRepository, times(1)).findByDocumentNumber(validGuestRequest.documentNumber());
    }

    @Test
    @DisplayName("Debe llamar al repositorio exactamente una vez para guardar")
    void shouldCallRepositoryOnceToSave() {
        // Given - Nuevo huésped
        when(guestRepository.findByDocumentNumber(validGuestRequest.documentNumber()))
                .thenReturn(Optional.empty());
        
        Guest savedGuest = new Guest(
                validGuestRequest.firstName(),
                validGuestRequest.lastName(),
                validGuestRequest.documentNumber(),
                validGuestRequest.email(),
                validGuestRequest.phone()
        );
        when(guestRepository.save(any(Guest.class))).thenReturn(savedGuest);

        // When
        guestService.registerOrUpdateGuest(validGuestRequest);

        // Then
        verify(guestRepository, times(1)).save(any(Guest.class));
    }

    // ============================================
    // Escenario: Diferentes tipos de datos
    // ============================================

    @Test
    @DisplayName("Debe manejar correctamente nombres con caracteres especiales")
    void shouldHandleSpecialCharactersInNames() {
        // Given - Request con caracteres especiales
        CreateGuestRequest specialRequest = new CreateGuestRequest(
                "José María",
                "O'Connor-Smith",
                "87654321",
                "jose.oconnor@example.com",
                "+34-666-123-456"
        );
        
        when(guestRepository.findByDocumentNumber(specialRequest.documentNumber()))
                .thenReturn(Optional.empty());
        
        Guest savedGuest = new Guest(
                specialRequest.firstName(),
                specialRequest.lastName(),
                specialRequest.documentNumber(),
                specialRequest.email(),
                specialRequest.phone()
        );
        when(guestRepository.save(any(Guest.class))).thenReturn(savedGuest);

        // When
        Guest result = guestService.registerOrUpdateGuest(specialRequest);

        // Then
        assertEquals("José María", result.getFirstName());
        assertEquals("O'Connor-Smith", result.getLastName());
    }

    @Test
    @DisplayName("Debe manejar correctamente diferentes formatos de teléfono")
    void shouldHandleDifferentPhoneFormats() {
        // Given - Diferentes formatos de teléfono
        CreateGuestRequest requestWithSpaces = new CreateGuestRequest(
                "John",
                "Doe",
                "11111111",
                "john@example.com",
                "+1 (555) 123-4567"
        );
        
        when(guestRepository.findByDocumentNumber(requestWithSpaces.documentNumber()))
                .thenReturn(Optional.empty());
        
        Guest savedGuest = new Guest(
                requestWithSpaces.firstName(),
                requestWithSpaces.lastName(),
                requestWithSpaces.documentNumber(),
                requestWithSpaces.email(),
                requestWithSpaces.phone()
        );
        when(guestRepository.save(any(Guest.class))).thenReturn(savedGuest);

        // When
        Guest result = guestService.registerOrUpdateGuest(requestWithSpaces);

        // Then
        assertEquals("+1 (555) 123-4567", result.getPhone());
    }

    @Test
    @DisplayName("Debe preservar el documento original al actualizar")
    void shouldPreserveDocumentNumberWhenUpdating() {
        // Given - Huésped existente
        String originalDocument = "DOC-12345";
        Guest existingGuestWithDoc = new Guest(
                "Jane",
                "Doe",
                originalDocument,
                "jane@example.com",
                "+1-555-9999"
        );
        
        CreateGuestRequest updateRequest = new CreateGuestRequest(
                "Jane Updated",
                "Doe Updated",
                originalDocument,
                "jane.updated@example.com",
                "+1-555-0000"
        );
        
        when(guestRepository.findByDocumentNumber(originalDocument))
                .thenReturn(Optional.of(existingGuestWithDoc));
        
        when(guestRepository.save(existingGuestWithDoc)).thenReturn(existingGuestWithDoc);

        // When
        Guest result = guestService.registerOrUpdateGuest(updateRequest);

        // Then - El documento no cambió
        assertEquals(originalDocument, result.getDocumentNumber());
        assertEquals("Jane Updated", result.getFirstName());
        assertEquals("Doe Updated", result.getLastName());
    }
}
