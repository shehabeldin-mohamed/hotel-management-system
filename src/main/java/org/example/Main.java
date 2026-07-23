package org.example;

import org.example.booking.Customer;
import org.example.booking.Reservation;
import org.example.booking.ReservationRoomType;
import org.example.booking.ReservationStatus;
import org.example.facility.CleaningTask;
import org.example.facility.MaintenanceRequest;
import org.example.facility.Room;
import org.example.facility.RoomType;
import org.example.gui.LoginWindow;
import org.example.hr.Admin;
import org.example.hr.EmploymentType;
import org.example.hr.Housekeeper;
import org.example.hr.Receptionist;
import org.example.hr.Technician;
import org.hibernate.Session;

import javax.swing.*;
import java.io.File;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class Main {

    public static void loadFirstTimeData() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            /* ==========================================================
             * 1. SYSTEM ADMIN
             * ========================================================== */
            Admin admin = new Admin("System", null, "Admin",
                    LocalDate.of(1990, 1, 1), "admin", "admin123");
            session.save(admin);

            /* ==========================================================
             * 2. HOTEL STAFF (Housekeepers, Technicians, Receptionists)
             * ========================================================== */
            Housekeeper housekeeper1 = new Housekeeper("Maria", null, "Garcia", LocalDate.of(1985, 5, 20),
                    "h12", "h123", EmploymentType.FullTime, LocalDate.now().minusYears(6), "555-0192",
                    5000.0, null, null, "Floor 1 - Suites", 12);

            Housekeeper housekeeper2 = new Housekeeper("Sophie", null, "Turner", LocalDate.of(1992, 3, 18),
                    "h13", "h456", EmploymentType.PartTime, LocalDate.now().minusYears(3), "555-0193",
                    null, 32.0, 18, "Floor 2 - Standard", 7);

            Technician technician1 = new Technician("John", "David", "Smith", LocalDate.of(1990, 8, 10),
                    "t12", "t123", EmploymentType.PartTime, LocalDate.now().minusYears(4), "555-8734",
                    null, 45.0, 20, "HVAC & Electrical", Set.of("Certified HVAC Technician", "Master Electrician"));

            Technician technician2 = new Technician("Carlos", null, "Rivera", LocalDate.of(1988, 11, 25),
                    "t13", "t456", EmploymentType.FullTime, LocalDate.now().minusYears(5), "555-8735",
                    4800.0, null, null, "Plumbing & Structural", Set.of("Licensed Plumber", "Structural Safety Inspector"));

            Receptionist receptionist1 = new Receptionist("Alice", null, "Wilson", LocalDate.of(1992, 3, 15),
                    "r12", "r123", EmploymentType.FullTime, LocalDate.now().minusYears(5), "555-0444",
                    5200.0, null, null, 101, Set.of("English", "Polish"));

            Receptionist receptionist2 = new Receptionist("James", null, "Carter", LocalDate.of(1989, 7, 30),
                    "r13", "r456", EmploymentType.FullTime, LocalDate.now().minusYears(7), "555-0445",
                    5500.0, null, null, 102, Set.of("English", "French", "Spanish"));

            session.save(housekeeper1);
            session.save(housekeeper2);
            session.save(technician1);
            session.save(technician2);
            session.save(receptionist1);
            session.save(receptionist2);

            /* ==========================================================
             * 3. ROOM TYPES & ROOMS
             * ========================================================== */
            RoomType suiteType = new RoomType("Presidential Suite", 500.0, 4, "Luxury suite with ocean view");
            Room room201 = new Room("201"); Room room202 = new Room("202");
            suiteType.addRoom(room201); suiteType.addRoom(room202);
            session.save(suiteType);

            RoomType standardType = new RoomType("Standard Double", 120.0, 2, "Standard room with two beds");
            Room room101 = new Room("101"); Room room102 = new Room("102"); Room room103 = new Room("103");
            standardType.addRoom(room101); standardType.addRoom(room102); standardType.addRoom(room103);
            session.save(standardType);

            RoomType deluxeType = new RoomType("Deluxe King", 250.0, 2, "Deluxe room with king-size bed");
            Room room301 = new Room("301"); Room room302 = new Room("302");
            deluxeType.addRoom(room301); deluxeType.addRoom(room302);
            session.save(deluxeType);

            RoomType familyType = new RoomType("Family Suite", 350.0, 6, "Spacious suite for families");
            Room room401 = new Room("401"); Room room402 = new Room("402");
            familyType.addRoom(room401); familyType.addRoom(room402);
            session.save(familyType);

            RoomType singleType = new RoomType("Single Economy", 80.0, 1, "Compact room for solo travellers");
            Room room501 = new Room("501");
            singleType.addRoom(room501); singleType.addRoom(new Room("502")); singleType.addRoom(new Room("503"));
            session.save(singleType);

            /* ==========================================================
             * 4. CUSTOMERS & RESERVATIONS
             * ========================================================== */

            // Bruce — completed stay (suite + standard), ended 10 days ago
            Customer bruce = new Customer("Bruce", "Thomas", "Wayne", LocalDate.of(1980, 2, 19), "bruce@gmail.com", 1500, "b123");
            Reservation res1 = new Reservation("RES-2026-001", LocalDate.now().minusDays(20), LocalDate.now().minusDays(15), LocalDate.now().minusDays(10), ReservationStatus.Confirmed);
            bruce.addReservation(res1);
            receptionist1.addReservation(res1);

            ReservationRoomType rrt1a = new ReservationRoomType(res1, suiteType, 4);
            ReservationRoomType rrt1b = new ReservationRoomType(res1, standardType, 2);
            res1.addReservationRoomType(rrt1a); suiteType.addReservationRoomType(rrt1a);
            res1.addReservationRoomType(rrt1b); standardType.addReservationRoomType(rrt1b);

            rrt1a.setAssignedRoom(room201);
            rrt1b.setAssignedRoom(room101);
            res1.createCheckIn(LocalDate.now().minusDays(15).atTime(14, 0), "Guest requested extra towels.");
            res1.changeReservationStatus(ReservationStatus.Completed);
            session.save(bruce);

            // Diana — confirmed upcoming (single economy + standard double, +30 days)
            Customer diana = new Customer("Diana", null, "Prince", LocalDate.of(1985, 7, 4), "diana@gmail.com", 500, "d123");
            Reservation res2 = new Reservation("RES-2026-002", LocalDate.now(), LocalDate.now().plusDays(30), LocalDate.now().plusDays(34), ReservationStatus.Confirmed);
            diana.addReservation(res2);

            ReservationRoomType rrt2a = new ReservationRoomType(res2, singleType, 1);
            ReservationRoomType rrt2b = new ReservationRoomType(res2, standardType, 2);
            res2.addReservationRoomType(rrt2a); singleType.addReservationRoomType(rrt2a);
            res2.addReservationRoomType(rrt2b); standardType.addReservationRoomType(rrt2b);
            session.save(diana);

            // Tony — confirmed (deluxe + suite, +55 days) + cancelled (standard, +85 days)
            Customer tony = new Customer("Tony", null, "Stark", LocalDate.of(1970, 5, 29), "tony@gmail.com", 2000, "t456");
            Reservation res3a = new Reservation("RES-2026-003", LocalDate.now(), LocalDate.now().plusDays(55), LocalDate.now().plusDays(61), ReservationStatus.Confirmed);
            Reservation res3b = new Reservation("RES-2026-004", LocalDate.now().minusDays(10), LocalDate.now().plusDays(85), LocalDate.now().plusDays(87), ReservationStatus.Cancelled);
            tony.addReservation(res3a);
            tony.addReservation(res3b);

            ReservationRoomType rrt3a = new ReservationRoomType(res3a, deluxeType, 2);
            ReservationRoomType rrt3c = new ReservationRoomType(res3a, suiteType, 4);
            ReservationRoomType rrt3b = new ReservationRoomType(res3b, standardType, 2);

            res3a.addReservationRoomType(rrt3a); deluxeType.addReservationRoomType(rrt3a);
            res3a.addReservationRoomType(rrt3c); suiteType.addReservationRoomType(rrt3c);
            res3b.addReservationRoomType(rrt3b); standardType.addReservationRoomType(rrt3b);
            session.save(tony);

            // Clark — completed past stay (family, -30 days) + confirmed upcoming (standard, +43 days)
            Customer clark = new Customer("Clark", null, "Kent", LocalDate.of(1984, 6, 18), "clark@gmail.com", 0, "c123");
            Reservation res4a = new Reservation("RES-2026-005", LocalDate.now().minusDays(40), LocalDate.now().minusDays(30), LocalDate.now().minusDays(25), ReservationStatus.Confirmed);
            Reservation res4b = new Reservation("RES-2026-006", LocalDate.now(), LocalDate.now().plusDays(43), LocalDate.now().plusDays(45), ReservationStatus.Confirmed);

            clark.addReservation(res4a);
            clark.addReservation(res4b);
            receptionist1.addReservation(res4a);

            ReservationRoomType rrt4a = new ReservationRoomType(res4a, familyType, 5);
            ReservationRoomType rrt4c = new ReservationRoomType(res4a, standardType, 2);
            ReservationRoomType rrt4b = new ReservationRoomType(res4b, standardType, 2);
            ReservationRoomType rrt4d = new ReservationRoomType(res4b, deluxeType, 2);

            res4a.addReservationRoomType(rrt4a); familyType.addReservationRoomType(rrt4a);
            res4a.addReservationRoomType(rrt4c); standardType.addReservationRoomType(rrt4c);
            res4b.addReservationRoomType(rrt4b); standardType.addReservationRoomType(rrt4b);
            res4b.addReservationRoomType(rrt4d); deluxeType.addReservationRoomType(rrt4d);

            rrt4a.setAssignedRoom(room401);
            rrt4c.setAssignedRoom(room103);
            res4a.createCheckIn(LocalDate.now().minusDays(30).atTime(14, 0), "Family celebration stay.");
            res4a.changeReservationStatus(ReservationStatus.Completed);
            session.save(clark);

            // Peter — confirmed upcoming (single economy, +13 days)
            Customer peter = new Customer("Peter", null, "Parker", LocalDate.of(2001, 8, 10), "peter@gmail.com", 750, "p123");
            Reservation res5 = new Reservation("RES-2026-007", LocalDate.now(), LocalDate.now().plusDays(13), LocalDate.now().plusDays(18), ReservationStatus.Confirmed);
            peter.addReservation(res5);
            ReservationRoomType rrt5 = new ReservationRoomType(res5, singleType, 1);
            res5.addReservationRoomType(rrt5); singleType.addReservationRoomType(rrt5);
            session.save(peter);

            // Steve — currently checked-in (deluxe, started 2 days ago, ends in 3 days)
            Customer steve = new Customer("Steve", null, "Rogers", LocalDate.of(1982, 4, 4), "steve@gmail.com", 300, "s123");
            Reservation res6 = new Reservation("RES-2026-008", LocalDate.now().minusDays(5), LocalDate.now().minusDays(2), LocalDate.now().plusDays(3), ReservationStatus.Confirmed);
            steve.addReservation(res6);
            receptionist2.addReservation(res6);

            ReservationRoomType rrt6a = new ReservationRoomType(res6, deluxeType, 2);
            ReservationRoomType rrt6b = new ReservationRoomType(res6, standardType, 2);
            res6.addReservationRoomType(rrt6a); deluxeType.addReservationRoomType(rrt6a);
            res6.addReservationRoomType(rrt6b); standardType.addReservationRoomType(rrt6b);

            rrt6a.setAssignedRoom(room301);
            rrt6b.setAssignedRoom(room102);
            res6.createCheckIn(LocalDate.now().minusDays(2).atTime(15, 0), "Early check-in approved.");
            session.save(steve);

            // Natasha — confirmed (suite, +69 days) + cancelled (family, +24 days)
            Customer natasha = new Customer("Natasha", null, "Romanoff", LocalDate.of(1988, 11, 22), "natasha@gmail.com", 0, "n123");
            Reservation res7a = new Reservation("RES-2026-009", LocalDate.now(), LocalDate.now().plusDays(69), LocalDate.now().plusDays(74), ReservationStatus.Confirmed);
            Reservation res7b = new Reservation("RES-2026-010", LocalDate.now().minusDays(15), LocalDate.now().plusDays(24), LocalDate.now().plusDays(28), ReservationStatus.Cancelled);
            natasha.addReservation(res7a);
            natasha.addReservation(res7b);

            ReservationRoomType rrt7a = new ReservationRoomType(res7a, suiteType, 2);
            ReservationRoomType rrt7c = new ReservationRoomType(res7a, deluxeType, 2);
            ReservationRoomType rrt7b = new ReservationRoomType(res7b, familyType, 3);

            res7a.addReservationRoomType(rrt7a); suiteType.addReservationRoomType(rrt7a);
            res7a.addReservationRoomType(rrt7c); deluxeType.addReservationRoomType(rrt7c);
            res7b.addReservationRoomType(rrt7b); familyType.addReservationRoomType(rrt7b);
            session.save(natasha);

            // Barry — completed past stay (standard, -60 days) + confirmed upcoming (single, +90 days)
            Customer barry = new Customer("Barry", null, "Allen", LocalDate.of(1995, 3, 14), "barry@gmail.com", 1200, "ba123");
            Reservation res8a = new Reservation("RES-2026-011", LocalDate.now().minusDays(70), LocalDate.now().minusDays(60), LocalDate.now().minusDays(57), ReservationStatus.Confirmed);
            Reservation res8b = new Reservation("RES-2026-012", LocalDate.now(), LocalDate.now().plusDays(90), LocalDate.now().plusDays(93), ReservationStatus.Confirmed);

            barry.addReservation(res8a);
            barry.addReservation(res8b);
            receptionist2.addReservation(res8a);

            ReservationRoomType rrt8a = new ReservationRoomType(res8a, standardType, 1);
            ReservationRoomType rrt8b = new ReservationRoomType(res8b, singleType, 1);
            ReservationRoomType rrt8c = new ReservationRoomType(res8b, standardType, 2);

            res8a.addReservationRoomType(rrt8a); standardType.addReservationRoomType(rrt8a);
            res8b.addReservationRoomType(rrt8b); singleType.addReservationRoomType(rrt8b);
            res8b.addReservationRoomType(rrt8c); standardType.addReservationRoomType(rrt8c);

            rrt8a.setAssignedRoom(room102);
            res8a.createCheckIn(LocalDate.now().minusDays(60).atTime(13, 0), "Quiet room requested.");
            res8a.changeReservationStatus(ReservationStatus.Completed);
            session.save(barry);

            /* ==========================================================
             * 5. HOUSEKEEPING & MAINTENANCE TASKS
             * ========================================================== */
            CleaningTask cleaning1 = new CleaningTask(housekeeper1, room201, "Deep clean after guest checkout.");
            CleaningTask cleaning2 = new CleaningTask(housekeeper1, room101, "Standard turnover clean.");
            CleaningTask cleaning3 = new CleaningTask(housekeeper2, room102, "Post-checkout deep clean.");
            CleaningTask cleaning4 = new CleaningTask(housekeeper2, room401, "Full suite clean after family stay.");

            housekeeper1.addCleaningTask(cleaning1); room201.addCleaningTask(cleaning1);
            housekeeper1.addCleaningTask(cleaning2); room101.addCleaningTask(cleaning2);
            housekeeper2.addCleaningTask(cleaning3); room102.addCleaningTask(cleaning3);
            housekeeper2.addCleaningTask(cleaning4); room401.addCleaningTask(cleaning4);

            session.save(cleaning1);
            session.save(cleaning2);
            session.save(cleaning3);
            session.save(cleaning4);

            MaintenanceRequest maintenance1 = new MaintenanceRequest(technician1, room101, "Fix leaky faucet.");
            MaintenanceRequest maintenance2 = new MaintenanceRequest(technician1, room301, "AC unit not cooling properly.");
            MaintenanceRequest maintenance3 = new MaintenanceRequest(technician2, room202, "Shower drain blocked.");
            MaintenanceRequest maintenance4 = new MaintenanceRequest(technician2, room402, "Bathroom tiles cracked.");

            technician1.addMaintenanceRequest(maintenance1); room101.addMaintenanceRequest(maintenance1);
            technician1.addMaintenanceRequest(maintenance2); room301.addMaintenanceRequest(maintenance2);
            technician2.addMaintenanceRequest(maintenance3); room202.addMaintenanceRequest(maintenance3);
            technician2.addMaintenanceRequest(maintenance4); room402.addMaintenanceRequest(maintenance4);

            session.save(maintenance1);
            session.save(maintenance2);
            session.save(maintenance3);
            session.save(maintenance4);

            // Finalize Transaction
            session.getTransaction().commit();
            printLoginCredentials();

        } catch (Exception e) {
            if (session.getTransaction() != null) session.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    /**
     * Helper method to output login details to the console after database generation.
     */
    private static void printLoginCredentials() {
        System.out.println("Sample data loaded successfully.");
        System.out.println("\n=== LOGIN CREDENTIALS ===");
        System.out.println("Admin         | ID:    admin             | Password: admin123");
        System.out.println("Receptionist1 | ID:    r12               | Password: r123");
        System.out.println("Receptionist2 | ID:    r13               | Password: r456");
        System.out.println("Housekeeper1  | ID:    h12               | Password: h123");
        System.out.println("Housekeeper2  | ID:    h13               | Password: h456");
        System.out.println("Technician1   | ID:    t12               | Password: t123");
        System.out.println("Technician2   | ID:    t13               | Password: t456");
        System.out.println("Customer      | Email: bruce@gmail.com   | Password: b123   (completed stay)");
        System.out.println("Customer      | Email: diana@gmail.com   | Password: d123   (confirmed upcoming)");
        System.out.println("Customer      | Email: tony@gmail.com    | Password: t456   (confirmed + cancelled)");
        System.out.println("Customer      | Email: clark@gmail.com   | Password: c123   (completed + confirmed)");
        System.out.println("Customer      | Email: peter@gmail.com   | Password: p123   (confirmed upcoming)");
        System.out.println("Customer      | Email: steve@gmail.com   | Password: s123   (currently checked-in)");
        System.out.println("Customer      | Email: natasha@gmail.com | Password: n123   (confirmed + cancelled)");
        System.out.println("Customer      | Email: barry@gmail.com   | Password: ba123  (completed + confirmed)");
    }

    public static void main(String[] args) {
        File dbFile = new File("data/hoteldb.mv.db");
        if (!dbFile.exists()) {
            loadFirstTimeData();
        }

        SwingUtilities.invokeLater(() -> new LoginWindow().setVisible(true));
    }
}
