package com.example.data.travel

data class FlightOffer(
    val id: String,
    val airline: String,
    val flightNumber: String,
    val origin: String,
    val destination: String,
    val departureTime: String,
    val arrivalTime: String,
    val duration: String,
    val stops: String,
    val baggageAllowance: String,
    val baseFare: Double,
    val taxesAndFees: Double,
    val totalFare: Double,
    val cabinClass: String,
    val bookingPartnerUrl: String
)

data class HotelOffer(
    val id: String,
    val name: String,
    val city: String,
    val starRating: Double,
    val pricePerNight: Double,
    val amenities: List<String>,
    val neighborhood: String,
    val bookingUrl: String
)

data class DestinationIdea(
    val name: String,
    val stateOrCountry: String,
    val bestDurationDays: Int,
    val budgetRange: String,
    val idealVibe: String,
    val highlights: List<String>
)

object TravelRepository {

    val popularDestinations = listOf(
        DestinationIdea(
            name = "Goa",
            stateOrCountry = "India",
            bestDurationDays = 3,
            budgetRange = "₹12,000 - ₹25,000",
            idealVibe = "Beach, Relaxation, Nightlife & Heritage",
            highlights = listOf("Anjuna Flea Market", "Fontainhas Latin Quarter", "Dudhsagar Waterfalls", "Palolem Beach")
        ),
        DestinationIdea(
            name = "Manali & Solang",
            stateOrCountry = "Himachal Pradesh",
            bestDurationDays = 4,
            budgetRange = "₹15,000 - ₹30,000",
            idealVibe = "Mountains, Snow, Trekking & Cafes",
            highlights = listOf("Solang Valley", "Old Manali Cafes", "Jogini Waterfall", "Atal Tunnel")
        ),
        DestinationIdea(
            name = "Munnar & Alleppey",
            stateOrCountry = "Kerala",
            bestDurationDays = 4,
            budgetRange = "₹16,000 - ₹32,000",
            idealVibe = "Tea Gardens, Houseboat & Ayurvedic Spas",
            highlights = listOf("Tea Plantations", "Backwater Houseboat Cruise", "Mattupetty Dam", "Eravikulam National Park")
        ),
        DestinationIdea(
            name = "Jaipur & Udaipur",
            stateOrCountry = "Rajasthan",
            bestDurationDays = 4,
            budgetRange = "₹14,000 - ₹28,000",
            idealVibe = "Royal Palaces, Culture & Lakeside Dinners",
            highlights = listOf("Amber Fort", "City Palace Udaipur", "Hawa Mahal", "Lake Pichola Boat Ride")
        )
    )

    fun searchFlights(origin: String, destination: String): List<FlightOffer> {
        val o = origin.uppercase().trim().ifEmpty { "DEL" }
        val d = destination.uppercase().trim().ifEmpty { "GOI" }

        return listOf(
            FlightOffer(
                id = "FL-601",
                airline = "IndiGo",
                flightNumber = "6E-2134",
                origin = o,
                destination = d,
                departureTime = "06:15 AM",
                arrivalTime = "08:45 AM",
                duration = "2h 30m",
                stops = "Non-stop",
                baggageAllowance = "15 kg Check-in + 7 kg Cabin",
                baseFare = 4120.0,
                taxesAndFees = 830.0,
                totalFare = 4950.0,
                cabinClass = "Economy",
                bookingPartnerUrl = "https://www.google.com/travel/flights?q=flights+from+$o+to+$d"
            ),
            FlightOffer(
                id = "FL-602",
                airline = "Air India",
                flightNumber = "AI-883",
                origin = o,
                destination = d,
                departureTime = "11:20 AM",
                arrivalTime = "02:00 PM",
                duration = "2h 40m",
                stops = "Non-stop",
                baggageAllowance = "25 kg Check-in + 7 kg Cabin (Free Meal Included)",
                baseFare = 4850.0,
                taxesAndFees = 950.0,
                totalFare = 5800.0,
                cabinClass = "Economy",
                bookingPartnerUrl = "https://www.google.com/travel/flights?q=flights+from+$o+to+$d"
            ),
            FlightOffer(
                id = "FL-603",
                airline = "Akasa Air",
                flightNumber = "QP-1309",
                origin = o,
                destination = d,
                departureTime = "04:45 PM",
                arrivalTime = "07:25 PM",
                duration = "2h 40m",
                stops = "Non-stop",
                baggageAllowance = "15 kg Check-in + 7 kg Cabin",
                baseFare = 3890.0,
                taxesAndFees = 790.0,
                totalFare = 4680.0,
                cabinClass = "Economy",
                bookingPartnerUrl = "https://www.google.com/travel/flights?q=flights+from+$o+to+$d"
            ),
            FlightOffer(
                id = "FL-604",
                airline = "Vistara / Air India",
                flightNumber = "UK-871",
                origin = o,
                destination = d,
                departureTime = "08:10 PM",
                arrivalTime = "10:40 PM",
                duration = "2h 30m",
                stops = "Non-stop",
                baggageAllowance = "20 kg Check-in + 7 kg Cabin",
                baseFare = 5400.0,
                taxesAndFees = 1050.0,
                totalFare = 6450.0,
                cabinClass = "Premium Economy",
                bookingPartnerUrl = "https://www.google.com/travel/flights?q=flights+from+$o+to+$d"
            )
        )
    }

    fun searchHotels(city: String): List<HotelOffer> {
        val c = city.trim().ifEmpty { "Goa" }
        return listOf(
            HotelOffer(
                id = "HT-101",
                name = "Azure Seaside Resort & Spa",
                city = c,
                starRating = 4.6,
                pricePerNight = 3200.0,
                amenities = listOf("Free WiFi", "Swimming Pool", "Complimentary Breakfast", "Ocean View"),
                neighborhood = "Calangute / Candolim",
                bookingUrl = "https://www.google.com/travel/hotels/$c"
            ),
            HotelOffer(
                id = "HT-102",
                name = "Heritage Boutique Villa",
                city = c,
                starRating = 4.8,
                pricePerNight = 2400.0,
                amenities = listOf("High Speed WiFi", "Garden Cafe", "Air Conditioning", "Scooter Rental Desk"),
                neighborhood = "Fontainhas Latin Quarter",
                bookingUrl = "https://www.google.com/travel/hotels/$c"
            ),
            HotelOffer(
                id = "HT-103",
                name = "Zostel & Nomad Co-Living Space",
                city = c,
                starRating = 4.5,
                pricePerNight = 950.0,
                amenities = listOf("Fast WiFi", "Dedicated Coworking", "Community Kitchen", "Rooftop Cafe"),
                neighborhood = "Anjuna Beach Rd",
                bookingUrl = "https://www.google.com/travel/hotels/$c"
            )
        )
    }

    val defaultPackingChecklist = listOf(
        "Government ID / Passport / Driving License",
        "Flight / Train Tickets & Hotel Confirmation",
        "Phone Charger, Power Bank & Cables",
        "Light Cotton Clothing & Beachwear",
        "Sunscreen (SPF 50+), Sunglasses & Hat",
        "Basic First-Aid Kit & Personal Medications",
        "Universal Adapter & Earphones",
        "Reusable Water Bottle",
        "Comfortable Walking Shoes & Flip-Flops"
    )
}
