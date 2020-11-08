/*
Bell Finder - A directory of English style bell towers

Copyright (C) 2020  Alan Sparrow

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package uk.org.freeflight.bellfinder

class CountyLookup {
    companion object {
        private val countyMap = mapOf(
            "AB" to "Alberta",
            "AL" to "Alabama",
            "AR" to "Arkansas",
            "ArgyllBute" to "Argyll & Bute",
            "BC" to "British Columbia",
            "Beds" to "Bedfordshire",
            "Berks" to "Berkshire",
            "Bl Gwent" to "Blaenau Gwent",
            "Bucks" to "Buckinghamshre",
            "C Aberdeen" to "Aberdeen",
            "C Bris" to "City of Bristol",
            "C Dundee" to "City of Dundee",
            "C Edin" to "City of Edinburgh",
            "C Glas" to "City of Glasgow",
            "C London" to "City of London",
            "CT" to "Connecticut",
            "Cambs" to "Cambridgeshire",
            "Carms" to "Carmarthanshire",
            "Clackman" to "Clackmannanshire",
            "DC" to "District of Columbia",
            "DE" to "Delaware",
            "Denbighs" to "Denbighshire",
            "Derbys" to "Derbyshire",
            "DumfGallwy" to "Dumfries & Galloway",
            "E Loth" to "East Lothian",
            "E Sussex" to "East Sussex",
            "EC" to "Eastern Cape",
            "ER Yorks" to "East Riding of Yorkshire",
            "FL" to "Florida",
            "Ferman" to "Fermanagh",
            "Flints" to "Flintshire",
            "GA" to "Georgia",
            "Gaut" to "Gauten",
            "Glos" to "Gloucestershire",
            "Gr London" to "Greater London",
            "Gr Man" to "Greater Manchester",
            "HI" to "Hawaii",
            "Hants" to "Hampshire",
            "Herefs" to "Herefordshire",
            "Herts" to "Hertfordshire",
            "IL" to "Illinois",
            "IoW" to "Isle of Wight",
            "KZN" to "KwaZulu-Natal",
            "Kilk" to "Kilkenny",
            "LA" to "Louisiana",
            "Lancs" to "Lancashire",
            "Leics" to "Leicestershire",
            "Lim" to "Limerick",
            "Lincs" to "Lincolnshire",
            "MA" to "Massachusetts",
            "MD" to "Maryland",
            "MI" to "Michigan",
            "Mers" to "Merseyside",
            "Merthyr" to "Merthyr Tydfil",
            "Monmths" to "Monmouthshire",
            "N Yorks" to "North Yorkshire",
            "NC" to "North Carolina",
            "NI" to "North Island",
            "NJ" to "New Jersey",
            "NSW" to "New South Wales",
            "NY" to "New York",
            "Neath PT" to "Neath Port Talbot",
            "North West" to "North West",
            "Northants" to "Northamptonshire",
            "Northumb" to "Northumberland",
            "Notts" to "Nottinghamshire",
            "ON" to "Ontario",
            "Oxon" to "Oxfordshire",
            "PA" to "Pennsylvania",
            "Pembs" to "Pembrokeshire",
            "PerthKross" to "Perth & Kinross",
            "QC" to "Quebec City",
            "Qld" to "Queensland",
            "RhonddaCT" to "Rhondda Cynon Taff",
            "S Yorks" to "South Yorkshire",
            "SA" to "South Australia",
            "SC" to "South Carolina",
            "SI" to "South Island",
            "Scilly" to "Isles of Scilly",
            "Shrops" to "Shropshire",
            "Som" to "Somerset",
            "Staffs" to "Staffordshire",
            "TN" to "Tennessee",
            "TX" to "Texas",
            "Tas" to "Tasmania",
            "Tip" to "Tipperary",
            "Tyne+Wear" to "Tyne & Wear",
            "VA" to "Virginia",
            "ValeGlam" to "Vale of Glamorgan",
            "Vic" to "Victoria",
            "W Mids" to "West Midlands",
            "W Sussex" to "West Sussex",
            "W Yorks" to "North Yorkshire",
            "WA" to "Western Australia",
            "WC" to "Western Cape",
            "Warks" to "Warwickshire",
            "Waterfd" to "Waterford",
            "Wilts" to "Wiltshire",
            "Worcs" to "Worcestershire",
        )

        fun lookup(abbr: String) : String {
            return countyMap[abbr] ?: abbr
        }
    }
}