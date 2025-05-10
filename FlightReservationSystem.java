import java.time.LocalDate;
import java.time.ZoneId;
import java.io.*;
public class FlightReservationSystem {
    public static void main(String[] args) {
        try {
            // === Example 1: Domestic flight booking ===
            long departure = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000); // 1 week later
            long arrival   = departure + (2L * 60 * 60 * 1000);                    // +2 hours

            DomesticFlight domesticFlight = new DomesticFlight(
                    "TK101", "Istanbul", "Ankara", departure, arrival, 1000.0, 0.18
            );
            long birth1 = System.currentTimeMillis() - (35L * 365 * 24 * 60 * 60 * 1000);
            Passenger p1 = new Passenger("Ahmet", "Yılmaz", birth1,
                    "A12345678", "Turkish",
                    "ahmet@example.com", "+905551234567");
            long birth2 = System.currentTimeMillis() - (28L * 365 * 24 * 60 * 60 * 1000);
            Passenger p2 = new Passenger("Ayşe", "Kaya", birth2,
                    "B98765432", "Turkish",
                    "ayse@example.com", "+905559876543");

            Passenger[] domesticPassengers = { p1, p2 };
            SeasonalPricingStrategy domPricing = new SeasonalPricingStrategy(0.18, 0.05);
            StandardBooking domBooking = new StandardBooking(domesticFlight, domesticPassengers, domPricing, true);

            boolean created1 = domBooking.createBooking();
            boolean seats1   = domBooking.assignSeats();

            System.out.println("=== Example 1 ===");
            System.out.println("Booking OK: " + created1 + ", Seats assigned: " + seats1);
            if (created1 && seats1) {
                CreditCardPayment payment1 = new CreditCardPayment(domBooking,
                        "1234567890123456", "Ahmet Yılmaz", "12/25", "123"
                );
                System.out.println("Payment OK: " + payment1.processPayment()
                        + ", PaymentStatus: " + payment1.getStatus());
                // Cancel & Refund Example
                if (domBooking.cancel()) {
                    double refund = domBooking.calculateRefundAmount();
                    payment1.refundPayment();
                    System.out.println("Cancelled & refunded: " + refund + " TL");
                }
            }

            // === Example 2: International flight booking ===
            long intlDep = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000); // 30 days later
            long intlArr = intlDep + (5L * 60 * 60 * 1000);                          // +5 hours

            InternationalFlight intlFlight = new InternationalFlight(
                    "TK202", "Istanbul", "Paris", intlDep, intlArr, 5000.0
            );
            long birth3 = System.currentTimeMillis() - (40L * 365 * 24 * 60 * 60 * 1000);
            Passenger p3 = new Passenger("Mehmet", "Çelik", birth3,
                    "C11223344", "Turkish",
                    "mehmet@example.com", "+905551112233");
            long birth4 = System.currentTimeMillis() - (30L * 365 * 24 * 60 * 60 * 1000);
            Passenger p4 = new Passenger("Elif", "Demir", birth4,
                    "D55667788", "Turkish",
                    "elif@example.com", "+905559998877");

            Passenger[] intlPassengers = { p3, p4 };
            SeasonalPricingStrategy intlPricing = new SeasonalPricingStrategy(0.20, 0.05);
            StandardBooking intlBooking = new StandardBooking(intlFlight, intlPassengers, intlPricing, false);

            boolean created2 = intlBooking.createBooking();
            boolean seats2   = intlBooking.assignSeats();
            System.out.println("\n=== Example 2 ===");
            System.out.println("Intl booking OK: " + created2 + ", Seats assigned: " + seats2);
            if (created2 && seats2) {
                CreditCardPayment payment2 = new CreditCardPayment(intlBooking,
                        "9876543210987654", "Elif Demir", "06/26", "456"
                );
                System.out.println("Payment OK: " + payment2.processPayment()
                        + ", Status: " + payment2.getStatus());
            }

            // === Example 3: Too many passengers (seat shortage) ===
            Passenger[] many = new Passenger[60];
            for (int i = 0; i < 60; i++) {
                many[i] = new Passenger("Test" + i, "User" + i, birth1,
                        "X" + i, "Country",
                        "test" + i + "@example.com", "+900000000000");
            }
            StandardBooking bigBooking = new StandardBooking(domesticFlight, many, domPricing, false);
            boolean created3 = bigBooking.createBooking();
            boolean seats3   = bigBooking.assignSeats();
            System.out.println("\n=== Example 3 ===");
            System.out.println("Big booking created: " + created3
                    + ", Seats assigned (should be false): " + seats3);

            // === Example 4: Change request too late ===
            long closeDep = System.currentTimeMillis() + (10L * 60 * 60 * 1000); // 10h later
            long closeArr = closeDep + (1L * 60 * 60 * 1000);                    // +1h
            DomesticFlight lateFlight = new DomesticFlight(
                    "TK103", "Istanbul", "Izmir", closeDep, closeArr, 500.0, 0.18
            );
            Passenger lateP = new Passenger("Test", "Late", birth1,
                    "L12345", "Turkish",
                    "late@example.com", "+900000000000");
            StandardBooking lateBooking = new StandardBooking(
                    lateFlight, new Passenger[]{lateP},
                    new SeasonalPricingStrategy(0.18,0.0), false
            );
            lateBooking.createBooking();
            lateBooking.assignSeats();
            ChangeRequest req = new ChangeRequest(
                    lateBooking.getBookingId(),
                    closeDep + (2L * 60 * 60 * 1000),
                    closeArr + (2L * 60 * 60 * 1000),
                    new String[]{"D1"}
            );
            System.out.println("\n=== Example 4 ===");
            System.out.println("Change allowed (should be false): "
                    + ((IChangeable) lateFlight).change(req));

            // === Example 5: Cancellation not allowed ===
            boolean cancelLate = lateBooking.cancel();
            System.out.println("\n=== Example 5 ===");
            System.out.println("Cancellation allowed (should be false): " + cancelLate);

            // === Example 6: Invalid payment details ===
            CreditCardPayment badPay = new CreditCardPayment(
                    lateBooking, "1111222233334444", "Late User", "01/20", "12"
            );
            System.out.println("\n=== Example 6 ===");
            System.out.println("Process bad payment (should be false): "
                    + badPay.processPayment());

            // === Example 7: Promo code application ===
            SeasonalPricingStrategy promoStrat = new SeasonalPricingStrategy(0.15, 0.0);
            boolean promoOK = promoStrat.applyPromoCode("SUMMER2023");
            double promoPrice = promoStrat.calculateFinalPrice(domesticFlight, 2);
            System.out.println("\n=== Example 7 ===");
            System.out.println("Promo applied: " + promoOK
                    + ", New price for 2 pax: " + promoPrice + " TL");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
enum FlightStatus {
    SCHEDULED,BOARDING,DEPARTED,ARRIVED,DELAYED,CANCELLED
}

enum BookingStatus {
    PENDING,CONFIRMED,CANCELLED,FAILED
}

enum PaymentStatus {
    PENDING, COMPLETED,FAILED,REFUNDED
}

enum ClassType {
    ECONOMY,BUSINESS,FIRST
}
abstract class AbstractFlight {
    protected String flightNumber;
    protected String origin;
    protected String destination;
    protected long departureTime;
    protected long arrivalTime;
    protected FlightStatus status;
    protected Seat[] seats;
    protected double basePrice;

    AbstractFlight(String flightNumber, String origin, String destination, long departureTime, long arrivalTime, double basePrice) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.basePrice = basePrice;
        this.status = FlightStatus.SCHEDULED;
        this.seats = initializeSeats();
    }
    protected abstract Seat[] initializeSeats();

    public String getFlightNumber() {
        return this.flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getOrigin() {
        return this.origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return this.destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public long getDepartureTime() {
        return this.departureTime;
    }

    public void setDepartureTime(long departureTime) {
        this.departureTime = departureTime;
    }

    public long getArrivalTime() {
        return this.arrivalTime;
    }

    public void setArrivalTime(long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public FlightStatus getStatus() {
        return this.status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    public Seat[] getSeats() {
        return this.seats;
    }

    public void setSeats(Seat[] seats) {
        this.seats = seats;
    }

    public double getBasePrice() {
        return this.basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }
    public long calculateDuration(){
        long duration = getArrivalTime() - getDepartureTime();
        return duration / (1000 * 60);
    }
    public Seat[] getAvailableSeats(){
        int count = 0;
        for(int i = 0 ; i < seats.length ; i++){
            if(seats[i].isAvailable()){
                count++;
            }
        }
        Seat[] availableSeats = new Seat[count];
        int index = 0;
        for(int i = 0 ; i < seats.length ; i++){
            if(seats[i].isAvailable()){
                availableSeats[index] = seats[i];
                index++;
            }
        }
        return availableSeats;
    }
}
abstract class AbstractBooking {
    protected String bookingId;
    protected AbstractFlight flight;
    protected Passenger[] passengers;
    protected Seat[] assignedSeats;
    protected long bookingTime;
    protected BookingStatus status;
    protected AbstractPricingStrategy pricingStrategy;
    protected double totalPrice;

    AbstractBooking(AbstractFlight flight, Passenger[] passengers, AbstractPricingStrategy pricingStrategy) {
        this.flight = flight;
        this.passengers = passengers;
        this.pricingStrategy = pricingStrategy;
        this.bookingTime = System.currentTimeMillis();
        this.status = BookingStatus.PENDING;
        this.bookingId = generateBookingId();
        this.assignedSeats = new Seat[passengers.length]; // ?
        this.totalPrice = calculateTotalPrice();
    }

    protected String generateBookingId (){
        return "BK" + System.currentTimeMillis();
    }
    public boolean createBooking (){
        try{
            totalPrice = pricingStrategy.calculateFinalPrice(flight,passengers.length);
            if(assignSeats()){
                status = BookingStatus.CONFIRMED;
                return true;
            }
            else{
                status = BookingStatus.FAILED;
                return false;
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            status = BookingStatus.FAILED;
            return false;
        }
    }
    public double calculateTotalPrice (){
        return pricingStrategy.calculateFinalPrice(flight,passengers.length);
    }
    public boolean assignSeats (){
        Seat[] availableSeats = flight.getAvailableSeats();;
        if(assignedSeats.length > availableSeats.length){
            return false;
        }
        for(int i = 0 ; i < passengers.length ; i++){
            if(availableSeats[i].reserve()){
                assignedSeats[i] = availableSeats[i];
            }
            else{
                return false;
            }
        }
        return true;
    }
    public void setBookingId(String bookingId){
        this.bookingId = bookingId;
    }
    public void setFlight(AbstractFlight flight){
        this.flight = flight;
    }
    public void setPassengers(Passenger[] passengers){
        this.passengers = passengers;
    }
    public void setAssignedSeats(Seat[] assignedSeats){
        this.assignedSeats = assignedSeats;
    }
    public void setBookingTime(long bookingTime){
        this.bookingTime = bookingTime;
    }
    public void setStatus(BookingStatus status){
        this.status = status;
    }
    public void setPricingStrategy(AbstractPricingStrategy pricingStrategy){
        this.pricingStrategy = pricingStrategy;
    }
    public void setTotalPrice(double totalPrice){
        this.totalPrice = totalPrice;
    }

    public String getBookingId() { return bookingId; }
    public AbstractFlight getFlight() { return flight; }
    public Passenger[] getPassengers() { return passengers; }
    public Seat[] getAssignedSeats() { return assignedSeats; }
    public long getBookingTime() { return bookingTime; }
    public BookingStatus getStatus() { return status; }
    public AbstractPricingStrategy getPricingStrategy() { return pricingStrategy; }
    public double getTotalPrice() { return totalPrice; }
}
abstract class AbstractPricingStrategy {
    protected double basePrice;
    protected double taxRate;
    protected double discountRate;
    public AbstractPricingStrategy(double taxRate ,double discountRate){
        this.taxRate = taxRate;
        this.discountRate = discountRate;
    }
    public double getBasePrice() {
        return this.basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getTaxRate() {
        return this.taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    public double getDiscountRate() {
        return this.discountRate;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }
    public double calculateBasePrice(AbstractFlight flight){
        return flight.getBasePrice();
    }
    public double applyDiscounts(int passengerCount){
        return getBasePrice() - getBasePrice() * discountRate;
    }
    public double applyTaxes (double priceAfterDiscount){
        double taxAmount = priceAfterDiscount * taxRate;
        return priceAfterDiscount + taxAmount;
    }
    public double calculateFinalPrice ( AbstractFlight flight , int passengerCount ){
        this.basePrice = calculateBasePrice(flight);
        double discountPerPerson = applyDiscounts(passengerCount);
        double taxedPricePerPerson = applyTaxes(discountPerPerson);
        return passengerCount  * taxedPricePerPerson;
    }

}
abstract class AbstractPaymentProcessor {
    protected String paymentId;
    protected AbstractBooking booking;
    protected double amount;
    protected long paymentTime;
    protected PaymentStatus status;
    public AbstractPaymentProcessor(AbstractBooking booking){
        this.paymentId = generatePaymentId();
        this.amount = booking.getTotalPrice();
        this.booking = booking;
        this.paymentTime = System.currentTimeMillis();
        this.status = PaymentStatus.PENDING;
    }
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public AbstractBooking getBooking() {
        return booking;
    }

    public void setBooking(AbstractBooking booking) {
        this.booking = booking;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(long paymentTime) {
        this.paymentTime = paymentTime;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    protected String generatePaymentId (){
        return "PAY" + System.currentTimeMillis();
    }
    public abstract boolean processPayment ();
    public abstract boolean validatePaymentDetails () ;
    public boolean refundPayment (){
        if(status == PaymentStatus.COMPLETED){
            setStatus(PaymentStatus.REFUNDED);
            return true;
        }
        return false;
    }
}
interface IChangeable {
    boolean change(ChangeRequest request);
    double calculateChangeFee();
    boolean isChangeAllowed();
}

interface IPricingStrategy {
    double getPriceForClass(ClassType type);
    double getPriceForDate(long date);
    boolean applyPromoCode(String code);
}

interface ICancellable {
    boolean cancel();
    double calculateCancellationFee();
    boolean isCancellationAllowed();
}

interface IRefundable {
    boolean processRefund();
    double calculateRefundAmount();
    boolean isRefundable();
}
class DomesticFlight extends AbstractFlight implements IChangeable {
    private double domesticTaxRate;
    public DomesticFlight(String flightNumber,String origin,String destination,long departureTime,long arrivalTime,double basePrice,double domesticTaxRate){
        super(flightNumber,origin,destination,departureTime,arrivalTime,basePrice);
        this.domesticTaxRate = domesticTaxRate;
    }
    public double getDomesticTaxRate() {
        return domesticTaxRate;
    }

    public void setDomesticTaxRate(double domesticTaxRate) {
        this.domesticTaxRate = domesticTaxRate;
    }

    @Override
    protected Seat [] initializeSeats (){
        Seat[] seats = new Seat[50];
        for(int i = 0 ; i < 10 ; i++){
            Seat business = new Seat("D" + (i+1),ClassType.BUSINESS);
            seats[i] = business;
        }
        for(int i = 10 ; i < 50 ; i++){
            Seat economy = new Seat("D" + (i+1),ClassType.ECONOMY);
            seats[i] = economy;
        }
        return seats;
    }
    public double calculateDomesticTax (){
        return getBasePrice() * getDomesticTaxRate();
    }
    @Override
    public boolean change ( ChangeRequest request ){
        if(isChangeAllowed()){
            setDepartureTime(request.getNewDepartureTime());
            setArrivalTime(request.getNewArrivalTime());
            return true;
        }
        return false;
    }
    @Override
    public double calculateChangeFee(){
        return getBasePrice() * 0.1;
    }
    @Override
    public boolean isChangeAllowed (){
        long currentTime = System.currentTimeMillis();
        return (getDepartureTime() - currentTime) / (1000 * 60 * 60) >= 24;
    }
}
class InternationalFlight extends AbstractFlight implements IChangeable {
    private final String[] requiredDocuments;
    public InternationalFlight( String flightNumber, String origin,String destination,long departureTime,long arrivalTime,double basePrice){
        super(flightNumber,origin,destination,departureTime,arrivalTime,basePrice);
        this.requiredDocuments = new String[] {
                "Passport", "Visa", "Vaccination Certificate"
        };
    }
    public String[] getRequiredDocuments() {
        return requiredDocuments;
    }
    @Override
    protected Seat [] initializeSeats (){
        Seat[] seats = new Seat[100];
        for(int i = 0 ; i < 10 ; i++){
            Seat first = new Seat("I" + (i+1),ClassType.FIRST);
            seats[i] = first;
        }
        for(int i = 10 ; i < 30 ; i++){
            Seat business = new Seat("I" + (i+1),ClassType.BUSINESS);
            seats[i] = business;
        }
        for(int i = 30 ; i < 100 ; i++){
            Seat economy = new Seat("I" + (i+1),ClassType.ECONOMY);
            seats[i] = economy;
        }
        return seats;
    }
    @Override
    public boolean change ( ChangeRequest request ){
        if(isChangeAllowed()){
            setDepartureTime(request.getNewDepartureTime());
            setArrivalTime(request.getNewArrivalTime());
            return true;
        }
        return false;
    }
    @Override
    public double calculateChangeFee (){
        return getBasePrice() * 0.2;
    }
    @Override
    public boolean isChangeAllowed (){
        long currentTime = System.currentTimeMillis();
        return (getDepartureTime() - currentTime) / (1000 * 60 * 60) >= 72;
    }
}
class StandardBooking extends AbstractBooking implements ICancellable, IRefundable {
    private boolean insuranceIncluded;
    public StandardBooking (AbstractFlight flight, Passenger [] passengers,AbstractPricingStrategy pricingStrategy,boolean insuranceIncluded){
        super(flight,passengers,pricingStrategy);
        this.insuranceIncluded = insuranceIncluded;
    }
    public boolean isInsuranceIncluded() {
        return insuranceIncluded;
    }

    public void setInsuranceIncluded(boolean insuranceIncluded) {
        this.insuranceIncluded = insuranceIncluded;
    }
    @Override
    public boolean cancel (){
        if(isCancellationAllowed()){
            setStatus(BookingStatus.CANCELLED);
            for(int i = 0 ; i < assignedSeats.length ; i++){
                if(assignedSeats[i] != null){
                    assignedSeats[i].release();
                }
            }
            return true;
        }
        return false;
    }
    @Override
    public double calculateCancellationFee (){
        if(insuranceIncluded){
            return 0;
        }
        double currentTime = System.currentTimeMillis();
        double time = (flight.getDepartureTime() - currentTime) / (1000 * 60 * 60);
        if(time > 30){
            return getTotalPrice() * 0.1;
        }
        else if(time >= 7 && time <= 30){
            return getTotalPrice() * 0.3;
        }
        else{
            return getTotalPrice() * 0.5;
        }
    }
    @Override
    public boolean isCancellationAllowed (){
        double currentTime = System.currentTimeMillis();
        return (flight.getDepartureTime() - currentTime) / (1000 * 60 * 60) >= 24;
    }
    @Override
    public boolean processRefund (){
        if(isRefundable()){
            double refundAmount = calculateRefundAmount();
            System.out.println("Refund process executed successfully.Refund amount: " + refundAmount);
            return true;
        }
        return false;
    }
    @Override
    public double calculateRefundAmount (){
        return getTotalPrice() - calculateCancellationFee();
    }
    @Override
    public boolean isRefundable (){
        return  status == BookingStatus.CANCELLED || (status == BookingStatus.CONFIRMED && isCancellationAllowed());
    }
}
class SeasonalPricingStrategy extends AbstractPricingStrategy implements IPricingStrategy {
    private final double lowSeasonRate;
    private final double highSeasonRate;
    private long[] highSeasonStartDates;
    private long[] highSeasonEndDates;
    public SeasonalPricingStrategy ( double taxRate , double discountRate ){
        super(taxRate,discountRate);
        this.lowSeasonRate = 0.8;
        this.highSeasonRate = 1.3;
        this.highSeasonStartDates = new long[2];
        highSeasonStartDates[0] = getTimestamp(6,15);
        highSeasonStartDates[1] = getTimestamp(9,15);
        this.highSeasonEndDates = new long[2];
        highSeasonEndDates[0] = getTimestamp(12,15);
        highSeasonEndDates[1] = getTimestamp(1,15);
    }
    private long getTimestamp(int month, int day) {
        LocalDate date = LocalDate.of(2025, month, day);
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    public double getLowSeasonRate() {
        return lowSeasonRate;
    }

    public double getHighSeasonRate() {
        return highSeasonRate;
    }

    public long[] getHighSeasonStartDates() {
        return highSeasonStartDates;
    }

    public long[] getHighSeasonEndDates() {
        return highSeasonEndDates;
    }
    private boolean isHighSeason(long date) {
        for (int i = 0; i < highSeasonStartDates.length; i++) {
            if (date >= highSeasonStartDates[i] && date <= highSeasonEndDates[i]) {
                return true;
            }
        }
        return false;
    }
    @Override
    public double calculateBasePrice ( AbstractFlight flight ){
        double basePrice = super.calculateBasePrice(flight);
        long date = flight.getDepartureTime();
        if(isHighSeason(date)){
            return basePrice * getHighSeasonRate();
        }
        return basePrice * getLowSeasonRate();
    }
    @Override
    public double getPriceForClass ( ClassType type ){
        if(type.equals(ClassType.FIRST)){
            return getBasePrice() *  3.0;
        }
        else if(type.equals(ClassType.BUSINESS)){
            return getBasePrice() * 2.0;
        }
        else{
            return getBasePrice();
        }
    }
    @Override
    public double getPriceForDate ( long date ){
        if(isHighSeason(date)){
            return getBasePrice() * getHighSeasonRate();
        }
        return getBasePrice() * getLowSeasonRate();
    }
    @Override
    public boolean applyPromoCode ( String code ){
        if(code.equals("SUMMER2023")){
            setDiscountRate(getDiscountRate() + 0.1);
            return true;
        }
        if(code.equals("WINTER2023")){
            setDiscountRate(getDiscountRate() + 0.15);
            return true;
        }
        return false;
    }
}
class CreditCardPayment extends AbstractPaymentProcessor {
    private String cardNumber;         // örn: "1234567812345678"
    private String cardHolderName;     // örn: "John Smith"
    private String expiryDate;         // örn: "12/25"
    private String cvv;                // örn: "123"

    public CreditCardPayment ( AbstractBooking booking , String cardNumber ,String cardHolderName ,String expiryDate , String cvv ){
        super(booking);
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
    @Override
    public boolean processPayment (){
        if(validatePaymentDetails()){
            setPaymentTime(System.currentTimeMillis());
            setStatus(PaymentStatus.COMPLETED);
            return true;
        }
        else{
            setStatus(PaymentStatus.FAILED);
            return false;
        }
    }
    @Override
    public boolean validatePaymentDetails (){
        if(getCardNumber() == null || getCardNumber().isEmpty() || getCardNumber().length() < 13 || getCardNumber().length() > 19){
            return false;
        }
        for(int i = 0 ; i < cardNumber.length() ; i++){
            char c = cardNumber.charAt(i);
            if(!Character.isDigit(c)){
                return false;
            }
        }
        if(getCardHolderName() == null || getCardHolderName().isEmpty() || !cardHolderName.contains(" ")){
            return false;
        }
        if (expiryDate == null ||  expiryDate.length() != 5 || expiryDate.charAt(2) != '/') {
            return false;
        }

        String monthPart = expiryDate.substring(0, 2);
        String yearPart = expiryDate.substring(3, 5);
        for (char c : monthPart.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        int month = Integer.parseInt(monthPart);
        if (month < 1 || month > 12) {
            return false;
        }
        if (cvv == null || (cvv.length() != 3 && cvv.length() != 4)) {
            return false;
        }

        for (char c : cvv.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;

    }
}
class Passenger {
    private final String id;
    private String firstName;
    private String lastName;
    private long birthDate;
    private String passportNumber;
    private String nationality;
    private String contactEmail;
    private String contactPhone;
    public Passenger ( String firstName , String lastName , long birthDate ,String passportNumber , String nationality ,String contactEmail , String contactPhone ){
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.passportNumber = passportNumber;
        this.nationality = nationality;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        int randomNumber = (int) (Math.random() * 1000);
        this.id = "P" + System.currentTimeMillis() + "-" + randomNumber;
    }
    public boolean validateDetails (){
        if(firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()){
            return false;
        }
        if(passportNumber == null || passportNumber.isEmpty() || passportNumber.length() < 5){
            return false;
        }
        if(contactEmail == null || contactEmail.isEmpty() || !contactEmail.contains("@")){
            return false;
        }
        if(contactPhone == null || contactPhone.isEmpty()){
            return false;
        }
        return true;
    }
    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public long getBirthDate() {
        return birthDate;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public String getNationality() {
        return nationality;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setBirthDate(long birthDate) {
        this.birthDate = birthDate;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public int getAge (){
        return (int) ((System.currentTimeMillis() - birthDate) / (1000L * 60 * 60 * 24 * 365));
    }
}
class Seat {
    private final String seatNumber;
    private final ClassType classType;
    private boolean available;
    public Seat ( String seatNumber , ClassType classType ){
        this.seatNumber = seatNumber;
        this.classType = classType;
        this.available = true;
    }
    public String getSeatNumber() {
        return seatNumber;
    }

    public ClassType getClassType() {
        return classType;
    }
    public void setAvailable(boolean available) {
        this.available = available;
    }
    public boolean isAvailable (){
      return available;
    }
    public boolean reserve (){
        if(!available){
            return false;
        }
        else{
            setAvailable(false);
            return true;
        }
    }
    public boolean release (){
        if(available){
            return false;
        }
        else{
            setAvailable(true);
            return true;
        }
    }
}
class ChangeRequest {
    private final String bookingId;
    private final long newDepartureTime;
    private final long newArrivalTime;
    private final String[] newSeatNumbers;
    public ChangeRequest ( String bookingId , long newDepartureTime , long newArrivalTime ,
                           String [] newSeatNumbers ){
        this.bookingId = bookingId;
        this.newDepartureTime = newDepartureTime;
        this.newArrivalTime = newArrivalTime;
        this.newSeatNumbers = newSeatNumbers;
    }
    public String getBookingId() {
        return bookingId;
    }

    public long getNewDepartureTime() {
        return newDepartureTime;
    }

    public long getNewArrivalTime() {
        return newArrivalTime;
    }

    public String[] getNewSeatNumbers() {
        return newSeatNumbers;
    }
}


