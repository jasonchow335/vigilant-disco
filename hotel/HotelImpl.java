package hotel;
import java.time.LocalDate;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public class HotelImpl implements Hotel {
    
    private ArrayList<Room> rooms;
    private ArrayList<Guest> guests;
    private ArrayList<Booking> bookings;
    private ArrayList<Payment> payments;

    /**
     * Load all the data from the four files.
     * 
     * @param roomsTxtFileName      the rooms txt file
     * @param guestsTxtFileName     the guests txt file
     * @param bookingsTxtFileName   the bookings txt file
     * @param paymentsTxtFileName   the payments txt file
     */
    public HotelImpl(String roomsTxtFileName, String guestsTxtFileName,
        String bookingsTxtFileName, String paymentsTxtFileName) {
            this.importRoomsData(roomsTxtFileName);
            this.importGuestsData(guestsTxtFileName);
            this.importBookingsData(bookingsTxtFileName);
            this.importPaymentsData(paymentsTxtFileName);
    }

    public boolean importRoomsData(String roomsTxtFileName) {
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(roomsTxtFileName));
        
            rooms = new ArrayList<>();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String[] room = line.split(",");
                Room r = new Room(Integer.parseInt(room[0]), room[1], Double.parseDouble(room[2]), room[3], room[4]);
                rooms.add(r);
            }

            bufferedReader.close();
            return true;

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            return false;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean importBookingsData(String bookingsTxtFileName) {
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(bookingsTxtFileName));
        
            bookings = new ArrayList<>();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String[] booking = line.split(",");
                Booking b = new Booking(Integer.parseInt(booking[0]), Integer.parseInt(booking[1]), Integer.parseInt(booking[2]),
                                        LocalDate.parse(booking[3]), LocalDate.parse(booking[4]), LocalDate.parse(booking[5]),
                                        Double.parseDouble(booking[6]));
                bookings.add(b);
            }

            bufferedReader.close();
            return true;

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            return false;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean importGuestsData(String guestsTxtFileName) {
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(guestsTxtFileName));
            
            guests = new ArrayList<>();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String[] guest = line.split(",");
                if (guest.length == 4) {
                    Guest g = new Guest(guest[1], guest[2], LocalDate.parse(guest[3]));
                    guests.add(g);
                } else {
                    VIPGuest g = new VIPGuest(guest[1], guest[2],
                                            LocalDate.parse(guest[3]), LocalDate.parse(guest[4]), LocalDate.parse(guest[5]));
                    guests.add(g);
                }
            }

            bufferedReader.close();
            return true;
            
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            return false;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean importPaymentsData(String paymentsTxtFileName) {
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(paymentsTxtFileName));
        
            payments = new ArrayList<>();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String[] payment = line.split(",");
                Payment p = new Payment(LocalDate.parse(payment[0]), Integer.parseInt(payment[1]), Double.parseDouble(payment[2]), payment[3]);
                payments.add(p);
            }

            bufferedReader.close();
            return true;

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            return false;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public void displayAllRooms() {
        for (int i=0; i<rooms.size(); i++) {
            System.out.println(rooms.get(i));
        }
    }

    public void displayAllGuests() {
        for (int i=0; i<guests.size(); i++) {
            System.out.println(guests.get(i));
        }
    }

    public void displayAllBookings() {
        for (int i=0; i<bookings.size(); i++) {
            System.out.println(bookings.get(i));
        }
    }

    public void displayAllPayments() {
        for (int i=0; i<payments.size(); i++) {
            System.out.println(payments.get(i));
        }
    }

    public boolean addRoom(int roomNumber, RoomType roomType, double price, int capacity, String facilities) {
        for(Room room : rooms) {
            if (room.getRoomNumber() == roomNumber){
                return false;
            }
        }
        Room room = new Room(roomNumber, roomTypeToString(roomType), price, Integer.toString(capacity), facilities);
        rooms.add(room);
        return true;
    }

    public boolean removeRoom(int roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber) {
                for (Booking booking : bookings) {
                    if (booking.getRoomNumber() == roomNumber) {
                        return false;
                    }
                }
                rooms.remove(room);
                return true;
            }
        }
        return false;
    }

    public boolean addGuest(String fName, String lName, LocalDate dateJoin) {
        Guest guest = new Guest(fName, lName, dateJoin);
        guests.add(guest);
        return true;
    }

    public boolean addGuest(String fName, String lName, LocalDate dateJoin,
        LocalDate VIPstartDate, LocalDate VIPexpiryDate) {

        // assert if VIPexpiryDate is one year after VIPstartdate
        assert((VIPexpiryDate.getYear() - VIPstartDate.getYear() == 1) && (VIPexpiryDate.getMonth() == VIPstartDate.getMonth())
         && (VIPexpiryDate.getDayOfMonth() == VIPstartDate.getDayOfMonth())) : "VIP membership must be 1 year.";

        VIPGuest guest = new VIPGuest(fName, lName, dateJoin, VIPstartDate, VIPexpiryDate);
        guests.add(guest);
        Payment payment = new Payment(VIPstartDate, guest.getGuestID(), 50.00, "VIPmembership");
        payments.add(payment);
        return true;
    }

    public boolean removeGuest(int guestID) {
        for (Guest guest : guests) {
            if (guest.getGuestID() == guestID) {
                // Check to see if the guest still has booking on any future days
                boolean noFutureBooking = true;
                for (Booking booking : bookings) {
                    if (booking.getGuestID() == guestID) {
                        if (booking.getCheckinDate().isAfter(LocalDate.now())) {
                            noFutureBooking = false;
                            break;
                        }
                    }
                }
                if (noFutureBooking) {
                    guests.remove(guest);
                    return true;
                }
                break;
            }
        }
        return false;
    }

    public boolean isAvailable(int roomNumber, LocalDate checkin, LocalDate checkout) {
        if (!checkin.isBefore(checkout)) {
            throw new IllegalArgumentException("Check-out date must be before check-in date");
        }
        for (Booking booking : bookings) {
            if (booking.getRoomNumber() == roomNumber){

                //Checks to see if the room has already been booked
                LocalDate max = (booking.getCheckouDate().isAfter(checkout)) ? booking.getCheckouDate() : checkout;
                LocalDate min = (booking.getCheckinDate().isBefore(checkin)) ? booking.getCheckinDate() : checkin;
                long min2max = ChronoUnit.DAYS.between(min, max);
                long booked = ChronoUnit.DAYS.between(booking.getCheckinDate(), booking.getCheckouDate());
                long stay = ChronoUnit.DAYS.between(checkin, checkout);
                if (min2max - (booked + stay) < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public int[] availableRooms(RoomType roomType, LocalDate checkin, LocalDate checkout) {
        ArrayList<Integer> roomNumbers = new ArrayList<>();
        String roomTypeStr = roomTypeToString(roomType);
        for (Room room : rooms) {
            if (room.getRoomType().equals(roomTypeStr)) {
                roomNumbers.add(room.getRoomNumber());
            }
        }
        roomNumbers.removeIf(x -> !(isAvailable(x, checkin, checkout)));
        return roomNumbers.stream().mapToInt(i -> i).toArray();
    }

    public int bookOneRoom(int guestID, RoomType roomType, LocalDate checkin, LocalDate checkout) {
        boolean guestExists = false;
        boolean isVIP = false;
        for (Guest guest : guests) {
            if (guest.getGuestID() == guestID) {
                guestExists = true;
                if (guest instanceof VIPGuest) {
                    VIPGuest vGuest = (VIPGuest)guest;
                    if (isInRangeOf(LocalDate.now(), vGuest.getVIPstartDate(), vGuest.getVIPexpiryDate())) {
                        isVIP = true;
                    }
                }
                break;
            }
        }
        if (!guestExists) {
            throw new IllegalArgumentException("Invalid guest ID");
        }
        int[] roomsAvailable = availableRooms(roomType, checkin, checkout);
        if (roomsAvailable.length == 0) {
            return -1;
        }
        int roomNo = roomsAvailable[new Random().nextInt(roomsAvailable.length)];
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNo) {
                int bookingID = bookings.get(bookings.size()-1).getId() + 1;

                // Calculate the price of the room for the guest
                double totalAmount = room.getPrice() * ChronoUnit.DAYS.between(checkin, checkout);
                if (isVIP) {totalAmount = 0.9 * totalAmount;}

                Booking booking = new Booking(bookingID, guestID, roomNo, LocalDate.now(), checkin, checkout, totalAmount);
                bookings.add(booking);
                Payment payment = new Payment(LocalDate.now(), guestID, totalAmount, "booking");
                payments.add(payment);
                break;
            }
        }
        return roomNo;
    }

    public boolean checkOut(int bookingID, LocalDate actualCheckoutDate) {
        for (Booking booking : bookings) {
            if (booking.getId() == bookingID){
                if (actualCheckoutDate.isAfter(booking.getCheckouDate())||(actualCheckoutDate.isBefore(booking.getCheckinDate()))) {
                    return false;
                }
                bookings.remove(booking);
                return true;
            }
        }
        System.out.println("Booking not found");
        return false;
    }

    public boolean cancelBooking(int bookingID) {
        for (Booking booking : bookings) {
            if (booking.getId() == bookingID) {
                bookings.remove(booking);

                // See if the guests can gather refunds
                LocalDate today = LocalDate.now();
                LocalDate checkinDate = booking.getCheckinDate();
                long days = ChronoUnit.DAYS.between(today, checkinDate);
                if (days >= 2) {
                    Payment payment = new Payment(today, booking.getGuestID(), -booking.getTotalAmount(), "refund");
                    payments.add(payment);
                }
                return true;
            }
        }
        return false;
    }

    public int[] searchGuest(String firstName, String lastName) {
        ArrayList<Integer> searchedGuests = new ArrayList<>();
        for (Guest guest : guests) {
            if ((guest.getFName().toLowerCase().equals(firstName.toLowerCase())) 
            && (guest.getLName().toLowerCase().equals(lastName.toLowerCase()))){
                searchedGuests.add(guest.getGuestID());
            }
        }
        return searchedGuests.stream().mapToInt(i -> i).toArray();
    }

    /** 
     * Searches for a guest using the first name and last name
     * Display their basic information booking information
     */
    public void searchAndDisplay(String firstName, String lastName){
        for (int guestID : searchGuest(firstName, lastName)) {
            for (Guest guest : guests) {
                if (guest.getGuestID() == guestID) {
                    System.out.println(guest.getFName() + " " + guest.getLName());
                    break;
                }
            }
            displayGuestBooking(guestID);
        }
    }

    public void displayGuestBooking(int guestID) {
        for (Booking booking : bookings) {
            if (booking.getGuestID() == guestID) {
                System.out.println(booking);
            }
        }
    }

    public void displayBookingsOn(LocalDate thisDate) {
        for (Booking booking: bookings){
            if (isInRangeOf(thisDate, booking.getCheckinDate(), booking.getCheckouDate())) {
                System.out.println(booking);
            }
        }
    }

    public void displayPaymentsOn(LocalDate thisDate) {
        for (Payment payment : payments) {
            if (payment.getDate().isEqual(thisDate)) {
                System.out.println(payment);
            }
        }
    }

    public boolean saveRoomsData(String roomsTxtFileName) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(roomsTxtFileName));
            for (Room room : rooms) {
                bufferedWriter.write(room.toString());
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean saveGuestsData(String guestsTxtFileName) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(guestsTxtFileName));
            for (Guest guest : guests) {
                bufferedWriter.write(guest.toString());
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean saveBookingsData(String bookingsTxtFileName) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(bookingsTxtFileName));
            for (Booking booking : bookings) {
                bufferedWriter.write(booking.toString());
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean savePaymentsData(String paymentsTxtFileName){
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(paymentsTxtFileName));
            for (Payment payment : payments) {
                bufferedWriter.write(payment.toString());
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**
     * Represents a Room object
     */
    static class Room {
        private int roomNumber;
        private String roomType;
        private double price;
        private String capacity;
        private String facilities;

        public int getRoomNumber() {return roomNumber;}
        public String getRoomType() {return roomType;}
        public double getPrice() {return price;}

        Room(int roomNumber, String roomType, double price, String capacity, String facilities) {
            this.roomNumber = roomNumber;
            this.roomType = roomType;
            this.price = price;
            this.capacity = capacity;
            this.facilities = facilities;
        }

        public String toString() {return Integer.toString(roomNumber) + "," + roomType + ","
            + String.format("%.2f", price) + "," + capacity + "," + facilities;
        }
    }

    /**
     * Represents a Guest object
     */
    static class Guest {
        private int guestID;
        private String fName;
        private String lName;
        private LocalDate dateJoin;
        private static int numberOfGuests = 0;

        public int getGuestID() {return guestID;}
        public String getFName() {return fName;}
        public String getLName() {return lName;}

        Guest(String fName, String lName, LocalDate dateJoin){
            this.fName = fName;
            this.lName = lName;
            this.dateJoin = dateJoin;
            numberOfGuests += 1;
            guestID = 10000 + numberOfGuests;
        }

        public String toString() {
            return Integer.toString(guestID) + "," + fName + "," + lName + ","
                    + dateJoin.toString();
        }
    }

    /**
     * Represents a VIPGuest object
    */
    static class VIPGuest extends Guest {
        private LocalDate VIPstartDate;
        private LocalDate VIPexpiryDate;

        public LocalDate getVIPstartDate() {return VIPstartDate;}
        public LocalDate getVIPexpiryDate() {return VIPexpiryDate;}

        VIPGuest(String fName, String lName, LocalDate dateJoin, LocalDate VIPstartDate, LocalDate VIPexpiryDate ){
            super(fName, lName, dateJoin);
            this.VIPstartDate = VIPstartDate;
            this.VIPexpiryDate = VIPexpiryDate;
        }

        @Override
        public String toString(){
            return super.toString()+ "," + VIPstartDate.toString() + "," + VIPexpiryDate.toString();
        }
    }

    /**
     * Represents a Booking object
     */
    static class Booking {
        private int id;
        private int guestID;
        private int roomNumber;
        private LocalDate bookingDate;
        private LocalDate checkinDate;
        private LocalDate checkoutDate;
        private double totalAmount;

        public int getId() {return id;}
        public int getGuestID() {return guestID;}
        public int getRoomNumber() {return roomNumber;}
        public LocalDate getBookingDate() {return bookingDate;}
        public LocalDate getCheckinDate() {return checkinDate;}
        public LocalDate getCheckouDate() {return checkoutDate;}
        public double getTotalAmount() {return totalAmount;}

        Booking(int id, int guestID, int roomNumber, LocalDate bookingDate, LocalDate checkinDate, LocalDate checkoutDate, double totalAmount) {
            this.id = id;
            this.guestID = guestID;
            this.roomNumber = roomNumber;
            this.bookingDate = bookingDate;
            this.checkinDate = checkinDate;
            this.checkoutDate = checkoutDate;
            this.totalAmount = totalAmount;
        }

        public String toString() {
            return Integer.toString(id) + "," + Integer.toString(guestID) + "," + Integer.toString(roomNumber)
            + "," + bookingDate.toString() + "," + checkinDate.toString() + "," + checkoutDate.toString()
            + "," + String.format("%.2f", totalAmount);
        }
    }

    /**
     * Represents a Payment object
     */
    static class Payment {
        private LocalDate date;
        private int guestID;
        private double amount;
        private String payReason;

        public LocalDate getDate() {return date;}
        public int getGuestID() {return guestID;}
        public double getAmount()  {return amount;}
        public String getPayReason() {return payReason;}

        Payment(LocalDate date, int guestID, double amount, String payReason) {
            this.date = date;
            this.guestID = guestID;
            this.amount = amount;
            this.payReason = payReason;
        }

        public String toString() {return date.toString() + "," + Integer.toString(guestID) + ","
            + Double.toString(amount) + "," + payReason;
        }
    }

    /**
     * Returns the string representation of the RoomType constant
     * 
     * @param roomType  a room type
     * @return          the string representation of the room type
     */
    String roomTypeToString(RoomType roomType) {
        String str;
        switch(roomType) {
            case DOUBLE: str = "double"; break;
            case SINGLE: str = "single"; break;
            case FAMILY: str = "family"; break;
            case TWIN: str = "twin"; break;
            default: throw new IllegalArgumentException("Invalid room type");
        }
        return str;
    }

    /**
     * Determines if a given date is in range of aDate and bDate, where aDate is before bDate (inclusive)
     * 
     * @param thisDate  a given date
     * @param aDate     the earlier of the two dates
     * @param bDate     the later of the two dates
     * @return          true if the date is in range of aDate and bDate, false otherwise 
     */
    boolean isInRangeOf(LocalDate thisDate, LocalDate aDate, LocalDate bDate) {
        return ((bDate.isAfter(thisDate)) && (aDate.isBefore(thisDate)))
            || (bDate.equals(thisDate)) || (aDate.equals(thisDate));
    }
}