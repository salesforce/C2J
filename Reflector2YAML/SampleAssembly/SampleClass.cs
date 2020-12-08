using System;
using System.Collections.Generic;
using Newtonsoft.Json;
/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

namespace TestAssembly
{
    [JsonObject]
    public abstract class Employee    
    {
        //public - will serialize
        public String Name { get; set; }
        
        // will serialize with json name: FamilyName
        [JsonProperty("FamilyName")]
        public String SureName { get; set; }
        
        public DateTime StartDate { get; set; }
        // public - will serialize as double
        public double EmploymentDuration
        { get ;set; }
        
        //has JsonProperty attribute - will serialize as reference type  (double?)
        [JsonProperty]
        private double? UnemploymentDuration
        { get ;set; }
        
        //won't serialize
        [JsonIgnore]
        public TimeSpan? ActualDuration { get; set; }

        public DateTime? ActualStart { get; set; }

        [JsonProperty("Manager")]
        private Manager DirectManager { get; set; }
        
        [JsonProperty] 
        int NetPay { get; set; }

    }    
    
    [JsonObject]
    public class HRDepart     
    {    
        [JsonProperty]
        List<Employee> Employees { get; set; }    

        [JsonProperty]
        List<Manager> Managers { get; set; }
        
        [JsonIgnore]
        public string Address { get; set; }
    }    

    [JsonObject]
    public class Manager : Employee    
    {
        [JsonProperty]
        private List<Employee> Subordinates { get; set; }

        [JsonProperty]  // nullable -> reference type
        public int? Options { get; set; }
        [JsonProperty] 
        public DateTime DateOfInception { get; set; }
        [JsonProperty] // basic type -> value type
        public int Bonus { get; set; }
    }    
}