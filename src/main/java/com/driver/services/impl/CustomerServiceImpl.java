package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database

		//saving child
//		List<TripBooking> tripBookingList = customer.getTripBookingList();
//		tripBookingRepository2.saveAll(tripBookingList);
		customerRepository2.save(customer); // cascade--> tripBooking saved
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer = customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE).
		// If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

		//customer --> tripbooking
		//driver --? tripbooking
		List<Driver> driverList = driverRepository2.findAll();
		for(Driver driver : driverList){
			Cab cab = driver.getCab();
			if(cab.getAvailable()){
				TripBooking tripBooking = new TripBooking();
				tripBooking.setFromLocation(fromLocation);
				tripBooking.setToLocation(toLocation);
				tripBooking.setDistanceInKm(distanceInKm);
				tripBooking.setStatus(TripStatus.CONFIRMED);
				int bill = cab.getPerKmRate() * distanceInKm;
				tripBooking.setBill(bill);

				Customer customer = customerRepository2.findById(customerId).get();
				customer.getTripBookingList().add(tripBooking);

				driver.getTripBookingList().add(tripBooking);
				cab.setAvailable(false);
				driver.setCab(cab);

				tripBooking.setCustomer(customer);
				tripBooking.setDriver(driver);

				//trip will not saved twice because of primary key
				driverRepository2.save(driver);  // cascade --> tripbooking
				customerRepository2.save(customer); // cascade --> tripbooking

//				tripBookingRepository2.save(tripBooking);

				return tripBooking;
			}
		}
		throw new Exception("No cab available!");
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		Driver driver = tripBooking.getDriver();
		Customer customer = tripBooking.getCustomer();

		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.setBill(0);
		tripBooking.setDistanceInKm(0);

		tripBooking.getDriver().getCab().setAvailable(true);
		tripBookingRepository2.save(tripBooking);

//		tripBooking.setDriver(null);
//		tripBooking.setCustomer(null);
//		tripBooking.setToLocation(null);
//		tripBooking.setFromLocation(null);

//		driver.getTripBookingList().remove(tripBooking);
//		driver.getCab().setAvailable(true);
//		customer.getTripBookingList().remove(tripBooking);
//
//		//cascade
//		driverRepository2.save(driver);
//		customerRepository2.save(customer);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
//		Driver driver = tripBooking.getDriver();
//		Customer customer = tripBooking.getCustomer();

		tripBooking.setStatus(TripStatus.COMPLETED);
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBookingRepository2.save(tripBooking);

//		driver.getCab().setAvailable(true);
//		driverRepository2.save(driver);//cascade --> cab, tripbookin
	}
}
