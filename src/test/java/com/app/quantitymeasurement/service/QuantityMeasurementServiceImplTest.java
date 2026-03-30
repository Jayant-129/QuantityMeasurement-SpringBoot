package com.app.quantitymeasurement.service;

import com.app.quantitymeasurement.model.OperationType;
import com.app.quantitymeasurement.model.QuantityDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementEntity;
import com.app.quantitymeasurement.repository.QuantityMeasurementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * MOCKITO UNIT TEST — QuantityMeasurementServiceImpl
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * HOW MOCKITO WORKS (the four-step pattern from your notes):
 *
 *   STEP 1 — Create a fake (mock) of the dependency
 *             @Mock QuantityMeasurementRepository repository;
 *             → Mockito creates a fake repository. It does NOT touch any DB.
 *
 *   STEP 2 — Define what the fake should return
 *             when(repository.save(any())).thenReturn(fakeEntity);
 *             → "Whenever save() is called with anything, return fakeEntity"
 *
 *   STEP 3 — Inject the fake into the class under test
 *             @InjectMocks QuantityMeasurementServiceImpl service;
 *             → Mockito automatically wires the @Mock into the @InjectMocks class
 *
 *   STEP 4 — Call the real method and assert the result
 *             QuantityMeasurementDTO result = service.compare(thisQty, thatQty);
 *             assertEquals("true", result.getResultString());
 *
 *   STEP 5 (optional) — Verify the mock was actually called
 *             verify(repository, times(1)).save(any());
 *             → Confirms the service really did call repository.save()
 *
 * WHY NO DATABASE?
 *   The repository is a @Mock — a fake object. When the service calls
 *   repository.save(...), Mockito intercepts it and returns whatever we
 *   told it to return in the when(...).thenReturn(...) setup.
 *   No Spring context, no H2, no SQL — just pure logic testing.
 *
 * ANNOTATIONS USED:
 *   @ExtendWith(MockitoExtension.class)  — activates Mockito in JUnit 5
 *   @Mock                                — creates a fake of that type
 *   @InjectMocks                         — creates a real instance, injects @Mocks into it
 *   @BeforeEach                          — runs before every single @Test
 *   @DisplayName                         — human-readable test name in reports
 * ─────────────────────────────────────────────────────────────────────────────
 */
@ExtendWith(MockitoExtension.class)
class QuantityMeasurementServiceImplTest {

    // ── STEP 1: Create fake (mock) dependencies ───────────────────────────────

    /**
     * @Mock tells Mockito: "Create a fake QuantityMeasurementRepository."
     * This fake object does NOTHING by default — it returns null, 0, or empty
     * lists for every method, unless we define behavior with when().thenReturn().
     */
    @Mock
    private QuantityMeasurementRepository repository;

    /**
     * @InjectMocks tells Mockito: "Create a REAL QuantityMeasurementServiceImpl
     * and inject the @Mock fields into it automatically."
     * So service.repository = the fake repository above.
     */
    @InjectMocks
    private QuantityMeasurementServiceImpl service;

    // ── Reusable test data ────────────────────────────────────────────────────

    private QuantityDTO feetQty;           // 1.0 FEET
    private QuantityDTO inchesQty;         // 12.0 INCHES
    private QuantityDTO celsiusQty;        // 100.0 CELSIUS
    private QuantityDTO fahrenheitQty;     // 0.0 FAHRENHEIT (target unit)
    private QuantityDTO gallonQty;         // 1.0 GALLON
    private QuantityDTO milliliterQty;     // 3785.41 MILLILITER
    private QuantityDTO kilogramQty;       // 1.0 KILOGRAM (different type — for error tests)

    private QuantityMeasurementEntity fakeEntity;

    /**
     * @BeforeEach runs before EVERY @Test method.
     * We build fresh QuantityDTO objects and a fake entity here
     * so every test starts with clean, predictable data.
     */
    @BeforeEach
    void setUp() {
        // Length quantities
        feetQty        = new QuantityDTO(1.0,    "FEET",        "LengthUnit");
        inchesQty      = new QuantityDTO(12.0,   "INCHES",      "LengthUnit");

        // Temperature quantities
        celsiusQty     = new QuantityDTO(100.0,  "CELSIUS",     "TemperatureUnit");
        fahrenheitQty  = new QuantityDTO(0.0,    "FAHRENHEIT",  "TemperatureUnit");

        // Volume quantities
        gallonQty      = new QuantityDTO(1.0,    "GALLON",      "VolumeUnit");
        milliliterQty  = new QuantityDTO(3785.41,"MILLILITER",  "VolumeUnit");

        // Different type — used for incompatibility error tests
        kilogramQty    = new QuantityDTO(1.0,    "KILOGRAM",    "WeightUnit");

        // ── STEP 2: Build a fake entity that repository.save() will return ────
        //
        // The service calls repository.save(entity) at the end of every operation.
        // We can't let it hit a real DB, so we tell the mock:
        // "When save() is called with anything, return this fakeEntity."
        //
        // fakeEntity mirrors what a real saved record would look like.
        fakeEntity = new QuantityMeasurementEntity();
        fakeEntity.setId(1L);
        fakeEntity.setOperation(OperationType.COMPARE.name());
        fakeEntity.setThisValue(1.0);
        fakeEntity.setThisUnit("FEET");
        fakeEntity.setThisMeasurementType("LengthUnit");
        fakeEntity.setThatValue(12.0);
        fakeEntity.setThatUnit("INCHES");
        fakeEntity.setThatMeasurementType("LengthUnit");
        fakeEntity.setResultString("true");
        fakeEntity.setError(false);

        // ── STEP 2 (applied): Tell the mock what to return when save() is called
        //
        // any() is a Mockito argument matcher — it matches any object passed in.
        // This covers all test cases that call saveAndReturn() internally.
        lenient().when(repository.save(any(QuantityMeasurementEntity.class)))
        .thenReturn(fakeEntity);
    }
    


    // ════════════════════════════════════════════════════════════════════════════
    // COMPARE TESTS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test: 1 FOOT vs 12 INCHES → should be equal (true)
     *
     * The "Hello John" equivalent in your notes:
     *   mock returns fakeEntity          → like mock returns "John"
     *   service.compare() runs logic     → like service.getGreeting() builds "Hello " + name
     *   result.getResultString() = "true"→ like result = "Hello John"
     */
    @Test
    @DisplayName("compare — 1 foot equals 12 inches → result is true")
    void testCompare_FootEqualsInches_ReturnsTrue() {

        // ── STEP 4: Call the real service method ──────────────────────────────
        QuantityMeasurementDTO result = service.compare(feetQty, inchesQty);

        // ── STEP 5: Assert the result ─────────────────────────────────────────
        assertNotNull(result);
        assertEquals("true", result.getResultString());
        assertFalse(result.isError());
        assertEquals("COMPARE", result.getOperation());

        // ── STEP 5 (verify): Confirm the mock was actually called ─────────────
        // This checks that the service really did call repository.save()
        // If the service forgot to save, this would fail — catching bugs.
        verify(repository, times(1)).save(any(QuantityMeasurementEntity.class));
    }

    @Test
    @DisplayName("compare — 1 foot does NOT equal 1 inch → result is false")
    void testCompare_FootNotEqualOneInch_ReturnsFalse() {
        // Override fakeEntity result for this specific scenario
        fakeEntity.setResultString("false");
        fakeEntity.setThatValue(1.0);

        QuantityDTO oneInch = new QuantityDTO(1.0, "INCHES", "LengthUnit");
        QuantityMeasurementDTO result = service.compare(feetQty, oneInch);

        assertNotNull(result);
        assertEquals("false", result.getResultString());
        assertFalse(result.isError());

        verify(repository, times(1)).save(any(QuantityMeasurementEntity.class));
    }

    @Test
    @DisplayName("compare — 100°C vs 100°C → result is true (same value, same unit)")
    void testCompare_SameTemperature_ReturnsTrue() {
        fakeEntity.setOperation("COMPARE");
        fakeEntity.setResultString("true");

        QuantityDTO anotherCelsius = new QuantityDTO(100.0, "CELSIUS", "TemperatureUnit");
        QuantityMeasurementDTO result = service.compare(celsiusQty, anotherCelsius);

        assertNotNull(result);
        assertEquals("true", result.getResultString());
        assertFalse(result.isError());
    }

    @Test
    @DisplayName("compare — incompatible types (LengthUnit vs WeightUnit) → error saved")
    void testCompare_IncompatibleTypes_SavesErrorRecord() {
        // Override fakeEntity to simulate an error record being saved
        fakeEntity.setError(true);
        fakeEntity.setErrorMessage("compare Error: Cannot perform arithmetic between different measurement categories: LengthUnit and WeightUnit");
        fakeEntity.setResultString(null);

        QuantityMeasurementDTO result = service.compare(feetQty, kilogramQty);

        // The service catches the exception and saves an error record —
        // it does NOT throw. We assert the error fields on the returned DTO.
        assertNotNull(result);
        assertTrue(result.isError());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Cannot perform arithmetic"));

        // Even on error, the service must call save() — this is the key
        // audit requirement (intentionally no @Transactional)
        verify(repository, times(1)).save(any(QuantityMeasurementEntity.class));
    }


    // ════════════════════════════════════════════════════════════════════════════
    // CONVERT TESTS
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("convert — 100°C to °F → result is 212.0")
    void testConvert_CelsiusToFahrenheit_Returns212() {
        fakeEntity.setOperation("CONVERT");
        fakeEntity.setResultValue(212.0);
        fakeEntity.setResultUnit("FAHRENHEIT");
        fakeEntity.setThisValue(100.0);
        fakeEntity.setThisUnit("CELSIUS");
        fakeEntity.setThisMeasurementType("TemperatureUnit");

        QuantityMeasurementDTO result = service.convert(celsiusQty, fahrenheitQty);

        assertNotNull(result);
        assertEquals(212.0, result.getResultValue(), 0.001);
        assertEquals("FAHRENHEIT", result.getResultUnit());
        assertFalse(result.isError());
        assertEquals("CONVERT", result.getOperation());

        verify(repository, times(1)).save(any(QuantityMeasurementEntity.class));
    }

    @Test
    @DisplayName("convert — 1 foot to inches → result is 12.0")
    void testConvert_FootToInches_Returns12() {
        fakeEntity.setOperation("CONVERT");
        fakeEntity.setResultValue(12.0);
        fakeEntity.setResultUnit("INCHES");

        QuantityMeasurementDTO result = service.convert(feetQty, inchesQty);

        assertNotNull(result);
        assertEquals(12.0, result.getResultValue(), 0.001);
        assertEquals("INCHES", result.getResultUnit());
        assertFalse(result.isError());
    }


    // ════════════════════════════════════════════════════════════════════════════
    // ADD TESTS
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("add — 1 gallon + 3785.41 ml → result is 2.0 gallons")
    void testAdd_GallonPlusMilliliter_ReturnsTwoGallons() {
        fakeEntity.setOperation("ADD");
        fakeEntity.setResultValue(2.0);
        fakeEntity.setResultUnit("GALLON");
        fakeEntity.setResultMeasurementType("VolumeUnit");

        QuantityMeasurementDTO result = service.add(gallonQty, milliliterQty);

        assertNotNull(result);
        assertEquals(2.0, result.getResultValue(), 0.01);
        assertEquals("GALLON", result.getResultUnit());
        assertFalse(result.isError());
        assertEquals("ADD", result.getOperation());

        verify(repository, times(1)).save(any(QuantityMeasurementEntity.class));
    }

    @Test
    @DisplayName("add — 1 foot + 12 inches → result is 2.0 feet")
    void testAdd_FootPlusInches_ReturnsTwoFeet() {
        fakeEntity.setOperation("ADD");
        fakeEntity.setResultValue(2.0);
        fakeEntity.setResultUnit("FEET");

        QuantityMeasurementDTO result = service.add(feetQty, inchesQty);

        assertNotNull(result);
        assertEquals(2.0, result.getResultValue(), 0.001);
        assertEquals("FEET", result.getResultUnit());
        assertFalse(result.isError());
    }

    @Test
    @DisplayName("add with target — 1 foot + 12 inches, target INCHES → result is 24.0 inches")
    void testAdd_WithTargetUnit_FootPlusInches_Returns24Inches() {
        QuantityDTO targetInches = new QuantityDTO(0.0, "INCHES", "LengthUnit");

        fakeEntity.setOperation("ADD");
        fakeEntity.setResultValue(24.0);
        fakeEntity.setResultUnit("INCHES");

        QuantityMeasurementDTO result = service.add(feetQty, inchesQty, targetInches);

        assertNotNull(result);
        assertEquals(24.0, result.getResultValue(), 0.001);
        assertEquals("INCHES", result.getResultUnit());
        assertFalse(result.isError());
    }

    @Test
    @DisplayName("add — incompatible types → error is saved")
    void testAdd_IncompatibleTypes_SavesError() {
        fakeEntity.setError(true);
        fakeEntity.setErrorMessage("add Error: Cannot perform arithmetic between different measurement categories");

        QuantityMeasurementDTO result = service.add(feetQty, kilogramQty);

        assertTrue(result.isError());
        assertNotNull(result.getErrorMessage());
        verify(repository, times(1)).save(any(QuantityMeasurementEntity.class));
    }


    // ════════════════════════════════════════════════════════════════════════════
    // SUBTRACT TESTS
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("subtract — 2 feet minus 12 inches → result is 1.0 foot")
    void testSubtract_TwoFeetMinusInches_ReturnsOneFoot() {
        QuantityDTO twoFeet = new QuantityDTO(2.0, "FEET", "LengthUnit");

        fakeEntity.setOperation("SUBTRACT");
        fakeEntity.setResultValue(1.0);
        fakeEntity.setResultUnit("FEET");

        QuantityMeasurementDTO result = service.subtract(twoFeet, inchesQty);

        assertNotNull(result);
        assertEquals(1.0, result.getResultValue(), 0.001);
        assertEquals("FEET", result.getResultUnit());
        assertFalse(result.isError());
        assertEquals("SUBTRACT", result.getOperation());

        verify(repository, times(1)).save(any(QuantityMeasurementEntity.class));
    }

    @Test
    @DisplayName("subtract with target — 2 feet minus 12 inches, target INCHES → result is 12.0 inches")
    void testSubtract_WithTargetUnit_Returns12Inches() {
        QuantityDTO twoFeet     = new QuantityDTO(2.0, "FEET",   "LengthUnit");
        QuantityDTO targetInches = new QuantityDTO(0.0, "INCHES", "LengthUnit");

        fakeEntity.setOperation("SUBTRACT");
        fakeEntity.setResultValue(12.0);
        fakeEntity.setResultUnit("INCHES");

        QuantityMeasurementDTO result = service.subtract(twoFeet, inchesQty, targetInches);

        assertNotNull(result);
        assertEquals(12.0, result.getResultValue(), 0.001);
        assertEquals("INCHES", result.getResultUnit());
        assertFalse(result.isError());
    }


    // ════════════════════════════════════════════════════════════════════════════
    // DIVIDE TESTS
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("divide — 1 yard divided by 1 foot → result is 3.0")
    void testDivide_YardByFoot_ReturnsThree() {
        QuantityDTO yardQty = new QuantityDTO(1.0, "YARDS", "LengthUnit");
        QuantityDTO footQty = new QuantityDTO(1.0, "FEET",  "LengthUnit");

        fakeEntity.setOperation("DIVIDE");
        fakeEntity.setResultValue(3.0);
        fakeEntity.setResultUnit("YARDS");

        QuantityMeasurementDTO result = service.divide(yardQty, footQty);

        assertNotNull(result);
        assertEquals(3.0, result.getResultValue(), 0.001);
        assertFalse(result.isError());
        assertEquals("DIVIDE", result.getOperation());

        verify(repository, times(1)).save(any(QuantityMeasurementEntity.class));
    }

    @Test
    @DisplayName("divide — divide by zero → error is saved with 'Divide by zero' message")
    void testDivide_ByZero_SavesErrorRecord() {
        QuantityDTO zeroinches = new QuantityDTO(0.0, "INCHES", "LengthUnit");

        fakeEntity.setError(true);
        fakeEntity.setErrorMessage("Divide by zero");

        QuantityMeasurementDTO result = service.divide(feetQty, zeroinches);

        // The service does NOT throw — it catches the ArithmeticException
        // and saves an error record. We verify error fields here.
        assertNotNull(result);
        assertTrue(result.isError());
        assertEquals("Divide by zero", result.getErrorMessage());

        // Even divide-by-zero must save to DB (audit requirement)
        verify(repository, times(1)).save(any(QuantityMeasurementEntity.class));
    }


    // ════════════════════════════════════════════════════════════════════════════
    // HISTORY / ANALYTICS TESTS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * getOperationHistory() calls repository.findByOperation().
     * Here we tell the mock: "When findByOperation('COMPARE') is called,
     * return a list containing fakeEntity."
     * Then we verify the service correctly converts it to a DTO list.
     */
    @Test
    @DisplayName("getOperationHistory — COMPARE → returns list of DTOs")
    void testGetOperationHistory_Compare_ReturnsList() {

        // ── STEP 2: Define behavior for this specific query method ────────────
        when(repository.findByOperation("COMPARE"))
                .thenReturn(List.of(fakeEntity));

        // ── STEP 4: Call the service ──────────────────────────────────────────
        List<QuantityMeasurementDTO> results = service.getOperationHistory("COMPARE");

        // ── STEP 5: Assert ────────────────────────────────────────────────────
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("COMPARE", results.get(0).getOperation());

        // Verify the right repository method was called with the right argument
        verify(repository, times(1)).findByOperation("COMPARE");
    }

    @Test
    @DisplayName("getOperationHistory — no records exist → returns empty list")
    void testGetOperationHistory_NoRecords_ReturnsEmptyList() {
        when(repository.findByOperation("ADD"))
                .thenReturn(Collections.emptyList());

        List<QuantityMeasurementDTO> results = service.getOperationHistory("ADD");

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(repository, times(1)).findByOperation("ADD");
    }

    @Test
    @DisplayName("getMeasurementsByType — LengthUnit → returns matching records")
    void testGetMeasurementsByType_LengthUnit_ReturnsList() {
        when(repository.findByThisMeasurementType("LengthUnit"))
                .thenReturn(List.of(fakeEntity));

        List<QuantityMeasurementDTO> results = service.getMeasurementsByType("LengthUnit");

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(repository, times(1)).findByThisMeasurementType("LengthUnit");
    }

    @Test
    @DisplayName("getOperationCount — COMPARE with 5 successes → returns 5")
    void testGetOperationCount_FiveCompareOperations_ReturnsFive() {

        // ── STEP 2: Define behavior ───────────────────────────────────────────
        // countByOperationAndIsErrorFalse returns a long (primitive)
        when(repository.countByOperationAndIsErrorFalse("COMPARE"))
                .thenReturn(5L);

        // ── STEP 4: Call service ──────────────────────────────────────────────
        long count = service.getOperationCount("COMPARE");

        // ── STEP 5: Assert ────────────────────────────────────────────────────
        assertEquals(5L, count);
        verify(repository, times(1)).countByOperationAndIsErrorFalse("COMPARE");
    }

    @Test
    @DisplayName("getOperationCount — no successful operations → returns 0")
    void testGetOperationCount_NoOperations_ReturnsZero() {
        when(repository.countByOperationAndIsErrorFalse("DIVIDE"))
                .thenReturn(0L);

        long count = service.getOperationCount("DIVIDE");

        assertEquals(0L, count);
    }

    @Test
    @DisplayName("getErrorHistory — one error record exists → returns it as DTO")
    void testGetErrorHistory_OneError_ReturnsErrorDTO() {
        // Build a dedicated error entity for this test
        QuantityMeasurementEntity errorEntity = new QuantityMeasurementEntity();
        errorEntity.setId(99L);
        errorEntity.setOperation("ADD");
        errorEntity.setThisValue(1.0);
        errorEntity.setThisUnit("FEET");
        errorEntity.setThisMeasurementType("LengthUnit");
        errorEntity.setThatValue(1.0);
        errorEntity.setThatUnit("KILOGRAM");
        errorEntity.setThatMeasurementType("WeightUnit");
        errorEntity.setError(true);
        errorEntity.setErrorMessage("add Error: Cannot perform arithmetic between different measurement categories: LengthUnit and WeightUnit");

        when(repository.findByIsErrorTrue())
                .thenReturn(List.of(errorEntity));

        List<QuantityMeasurementDTO> results = service.getErrorHistory();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isError());
        assertTrue(results.get(0).getErrorMessage().contains("Cannot perform arithmetic"));

        verify(repository, times(1)).findByIsErrorTrue();
    }

    @Test
    @DisplayName("getErrorHistory — no errors → returns empty list")
    void testGetErrorHistory_NoErrors_ReturnsEmptyList() {
        when(repository.findByIsErrorTrue())
                .thenReturn(Collections.emptyList());

        List<QuantityMeasurementDTO> results = service.getErrorHistory();

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }


    // ════════════════════════════════════════════════════════════════════════════
    // VERIFY — checking HOW MANY TIMES a mock was called
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * This test specifically demonstrates verify() — confirming that
     * repository.save() is called exactly ONCE per operation.
     * If someone refactored the service to save twice, this test would catch it.
     */
    @Test
    @DisplayName("verify — repository.save() is called exactly once per compare operation")
    void testVerify_SaveCalledExactlyOnce_PerOperation() {
        service.compare(feetQty, inchesQty);

        // times(1) = must have been called exactly 1 time
        verify(repository, times(1)).save(any(QuantityMeasurementEntity.class));

        // never() = findByOperation must NOT have been called during compare
        verify(repository, never()).findByOperation(anyString());
    }

    /**
     * Demonstrates thenReturn() chaining — the mock returns different
     * values on successive calls. First call → 3, second call → 7.
     */
    @Test
    @DisplayName("verify — mock returns different values on successive calls")
    void testVerify_MockReturnsDifferentValuesOnSuccessiveCalls() {
        when(repository.countByOperationAndIsErrorFalse("COMPARE"))
                .thenReturn(3L)   // first call
                .thenReturn(7L);  // second call

        assertEquals(3L, service.getOperationCount("COMPARE")); // first call
        assertEquals(7L, service.getOperationCount("COMPARE")); // second call

        verify(repository, times(2)).countByOperationAndIsErrorFalse("COMPARE");
    }
}

