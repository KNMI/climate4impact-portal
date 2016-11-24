var esgfsearch_colorscheme_multicolor = {
  "background":"#000000",
  "color1":"#00a8ec",
  "color2":"#f58d00",
  "color3":"#d32c2c",
  "color4":"#dda606",
  "color5":"#6b6b6b",
  "color6":"#ffc808",
  "color7":"#640f6c"
  
};

var esgfsearch_colorscheme_gray = {
  "background":"#C5C6CD",
  "color1":"#6C6D6C",
  "color2":"#6C6D6C",
  "color3":"#6C6D6C",
  "color4":"#6C6D6C",
  "color5":"#6C6D6C",
  "color6":"#6C6D6C",
  "color7":"#6C6D6C"
};


var esgfsearch_colorscheme_blueblackwhite = {
  "background":"#B5B6BD",
  "color1":"#f58d00",
  "color2":"#00a8ec",
  "color3":"#4CAF50",
  "color4":"#dda606",
  "color5":"#AEB404",
  "color6":"#e35c5c",
  "color7":"#9268FF"
};

var esgfsearch_currentcolorscheme = esgfsearch_colorscheme_blueblackwhite;

var esgfsearch_pc_project={
   "tilewidth":"200px",
  "properties":[
    {
      "shortname": "CMIP5",
      "color":     esgfsearch_currentcolorscheme.color1,
      "tooltip":   "global relatively low resolution climate projection data",
      "children": [
                   {
                     "name":      "CMIP5",
                     "longname":  "Coupled Model Intercomparison Project Phase 5",
                     "shortname": "GCM data <a target=\"_blank\" href=\"http://cmip-pcmdi.llnl.gov/cmip5/\">CMIP5 project page</a>",
                     "tooltip":   "global climate projection data used for latest IPCC report (AR5 2014) "
                   },
                   {
                     /* can we insert a blank line so as to create two groups within the temperature box */
                   },
                   {
                     "name":      "NEXGDDP",
                     "longname":  "Daily Minimum Near-Surface Air Temperature",
                     "shortname": "GCM data <a target=\"_blank\" href=\"https://nex.nasa.gov/nex/projects/1356/\">NEXGDDP project page</a>",
                     "tooltip":   "bias adjusted CMIP5 data "
                   }
                 ]
    },
    {
      "shortname": "CORDEX",
      "color":     esgfsearch_currentcolorscheme.color2,
      "tooltip":   "regional high resolution climate projection data (using RCMs);organised by continent",
      "children": [
                   {
                     "name":      "CORDEX",
                     "shortname": "RCM data <a target=\"_blank\" href=\"http://www.cordex.org/\">CORDEX</a>",
                     "tooltip":   "regional dynamically downscaled data; organised by continent"
                   },
                   {
                     /* can we insert a blank line so as to create two groups within the temperature box */
                   },
                   {
                     "name":      "CORDEX-Adjust",
                     "shortname": "RCM data <a target=\"_blank\" href=\"http://www.cordex.org/index.php?option=com_content&view=article&id=275&Itemid=785/\">CORDEX-Adjust</a>",
                     "tooltip":   "bias adjusted CORDEX data; organised by continent "
                   }
                 ]
    },
    {
    "shortname": "OBSERVATIONS",
    "color":     esgfsearch_currentcolorscheme.color3,
    "tooltip":   "gridded observational data for model validation purposes, or to force impact models",
    "children": [
                 {
                   "name":      "obs4MIPs",
                   "longname":  "Observations for Model Intercomparison Projects<br/>Long term regional climate change runs</br>",
                   "shortname": "satellite data <a target=\"_blank\" href=\"http://https://www.earthsystemcog.org/projects/obs4mips/\">obs4MIPs project page</a>",
                   "tooltip":   "gridded observational data (mostly for model validation purposes)"
                 },
                 {
                   /* can we insert a blank line so as to create two groups within the temperature box */
                 },
                 {
                   "name":      "clipc",
                   "longname":  "Climate Information Platform for Copernicus<br/></br><br/>Historic observed climate</br><br/>",
                   "shortname": "station data <a target=\"_blank\" href=\"http://www.clipc.eu/\">CLIPC project page</a>",
                   "tooltip":   "gridded observational near surface data for model validation purposes, or to force impact models"
                 }
               ]
  },
    /*{
    "name":      "specs",
    "longname":  "Seasonal-to-decadal climate Prediction for the improvement of European Climate Services<br/></br><a target=\"_blank\" href=\"http://www.specs-fp7.eu/\">SPECS project page</a>",
    "shortname": "SPECS",
    "color":     esgfsearch_currentcolorscheme.color3,
    "tooltip":   "seasonal forecasting data (hindcasts actually, for verification purposes)"
  },*/
  /* specs hidden for now since data here are NOT suitable for S2D impact studies (relevant for S2D development only)*/
  ]
};

var esgfsearch_pc_time_frequency={
  "tilewidth":"170px",
  "properties":[
    {
      "name":      "3hr",
      "longname":  "Instantaneous states every 3 hours",
      "shortname": "3hr",
      "color":     esgfsearch_currentcolorscheme.color1,
      "tooltip":   "can be used as sub-daily forcing data, or for advanced change assessments"
    },{
      "name":      "6hr",
      "longname":  "Instantaneous states every 6 hours",
      "shortname": "6hr",
      "color":     esgfsearch_currentcolorscheme.color2,
      "tooltip":   "can be used as sub-daily forcing data, or for advanced change assessments"
    },{
      "name":      "day",
      "longname":  "Daily averages",
      "shortname": "daily",
      "color":     esgfsearch_currentcolorscheme.color3,
      "tooltip":   "mostly used as forcing data, or for advanced change assessments"
    },{
      "name":      "mon",
      "longname":  "Monthly averages",
      "shortname": "monthly",
      "color":     esgfsearch_currentcolorscheme.color5,
      "tooltip":   "can be used for basic change assessments"
    },{
      "name":      "year",
      "longname":  "Annual averages",
      "shortname": "yearly",
      "color":     esgfsearch_currentcolorscheme.color6,
      "tooltip":   "can be for basic change assessments"
    }
  ]
};


var esgfsearch_pc_experiments={
  "tilewidth":"300px",
  "properties":
  [
    {
      "longname":  "Historical",
      "shortname": "Historical",
      "color":     esgfsearch_currentcolorscheme.color2,
      "children": [
        {
          "name":      "historical",
          "longname":  "Historical",
          "shortname": "Historical",
          "tooltip":   "should be statistically similar to observed, NOT day-to-day (use AMIP data then)"
        }
      ]
    },{
      "longname":  "Representative concentration pathway",
      "shortname": "RCP",
      "color":     esgfsearch_currentcolorscheme.color1,
      "children": [
        {
          "name":      "rcp26",
          "longname":  "Representative concentration pathway with radiative forcing of 2.6 W m-2",
          "shortname": "Radiative forcing of 2.6 W m-2",
          "tooltip":   "most ambitious emission reduction scenario, leading to ~1K global warming in 2100"
        },
        {
          "name":      "rcp45",
          "longname":  "Representative concentration pathway with radiative forcing of 4.5 W m-2",
          "shortname": "Radiative forcing of 4.5 W m-2",
          "tooltip":   "medium high emission reduction scenario, leading to ~2K global warming in 2100"
        },
        {
          "name":      "rcp60",
          "longname":  "Representative concentration pathway with radiative forcing of 6.0 W m-2",
          "shortname": "Radiative forcing of 6.0 W m-2",
          "tooltip":   "medium low emission reduction scenario, leading to ~ 3K global warming in 2100"
        },
        {
          "name":      "rcp85",
          "longname":  "Representative concentration pathway with radiative forcing of 8.5 W m-2",
          "shortname": "Radiative forcing of 8.5 W m-2",
          "tooltip":   "business as usual emission scenario, leading to >4K global warming in 2100"
        }
      ]
    }
  ]
}



var esgfsearch_pc_variables={ 
  "properties":[

    {
      "longname":    "Air Temperature",
      "shortname":   "Temperature",
      "weathericon": "wi wi-thermometer-exterior",
      "color":       esgfsearch_currentcolorscheme.color1,
      "children": [
        {
          "name":      "tas",
          "longname":  "Near-Surface Air Temperature",
          "shortname": "Temperature",
          "tooltip":   "2m temperature, as in observed station data "
        },{
          "name":      "tasmin",
          "longname":  "Daily Minimum Near-Surface Air Temperature",
          "shortname": "Min. Temperature",
          "tooltip":   "2m minimum temperature, as in observed station data "
        },
        {
          "name":      "tasmax",
          "longname":  "Daily Maximum Near-Surface Air Temperature",
          "shortname": "Max. Temperature",
          "tooltip":   "2m maximum temperature, as in observed station data "
        },
        {
          /* can we insert a blank line so as to create two groups within the temperature box */
        },
        {
          "name":      "ta",
          "longname":  "Air Temperature",
          "shortname": "Air Temperature",
          "tooltip":   "air temperature at different model levels/altitudes"
        }
      ]
    },
    {
      "name":        "pr",
      "longname":    "Total Precipitation",
      "shortname":   "Precipitation",
      "weathericon": "wi wi-rain",
      "color":       esgfsearch_currentcolorscheme.color2,
      "children": [ 
        {
          "name":      "pr",
          "longname":  "Total Precipitation",
          "shortname": "Precip.",
          "tooltip":   "precipitation (all liquid and solid forms), as in observed station data "
        },
        {
          "name":      "prc",
          "longname":  "Convective Precipitation",
          "shortname": "Conv. Precip.",
          "tooltip":   "convective precipitation (so stratiform precipitation = pr-prc)"
        },
        {
          "name":      "prsn",
          "longname":  "Snowfall",
          "shortname": "Snow",
          "tooltip":   "snow fall, as in observed station data (so rainfall = pr-prsn)"
        }
      ]
    },
    {
    
      "shortname":   "Humidity",
      "weathericon": "wi wi-humidity",
      "color":       esgfsearch_currentcolorscheme.color3,
      "children": [
        {
          "name":      "huss",
          "longname":  "Near-Surface Specific Humidity",
          "shortname": "Specific Humidity",
          "tooltip":   "2m specific humidity, as in observed station data "
        },
        {
          "name":      "hurs",
          "longname":  "Near-Surface Relative Humidity",
          "shortname": "Rel. Humidity",
          "tooltip":   "2m relative humidity, as in observed station data "
        },
        {
          "name":      "rhsmax",
          "longname":  "Daily Maximum Near-Surface Relative Humidity",
          "shortname": "Max. Rel. Humidity",
          "tooltip":   "2m daily maximum rel. humidity, as in observed station data "
        },
        {
          "name":      "rhsmin",
          "longname":  "Daily Minimium Near-Surface Relative Humidity",
          "shortname": "Min. Rel. Humidity",
          "tooltip":   "2m daily minimum rel. humidity, as in observed station data "
        },
        {
          "name":      "rhs",
          "longname":  "Daily Near-Surface Specific Humidity",
          "shortname": "Rel. Humidity",
          "tooltip":   "2m daily mean rel. humidity, as in observed station data "
        },
        {
          "name":      "hus",
          "longname":  "Specific Humidity",
          "shortname": "Spec. Humidity",
          "tooltip":   "specific humidity at different model levels/altitudes"
        },
        {
          "name":      "hur",
          "longname":  "Relative Humidity",
          "shortname": "Rel. Humidity",
          "tooltip":   "relative humidity at different model levels/altitudes"
        }
      ]
    },
    {
      "shortname":   "Wind",
      "weathericon": "wi wi-strong-wind",
      "color":       esgfsearch_currentcolorscheme.color5,
      "children": [
        {
          "name":      "sfcWind",
          "longname":  "Near-Surface Wind Speed",
          "shortname": "Wind",
          "tooltip":   "10m wind speed, as in observed station data "
        },
        {
          "name":      "sfcWindmax",
          "longname":  "Daily Maximum Near-Surface Wind Speed",
          "shortname": "Max. Wind",
          "tooltip":   "10m dail max wind speed, as in observed station data "
        },
        {
          /* can we insert a blank line so as to create two groups within the temperature box */
        },
        {
          "name":      "uas",
          "longname":  "Eastward Near-Surface Wind",
          "shortname": "E. Wind",
          "tooltip":   "eastward wind speed at different model levels/altitudes"
        },
        {
          "name":      "vas",
          "longname":  "Northward Near-Surface Wind Speed",
          "shortname": "N. Wind",
          "tooltip":   "northward wind speed at different model levels/altitudes"
                     }
      ]
    },
    {
      "shortname":   "Radiation",
      "weathericon": "wi wi-day-sunny",
      "color":       esgfsearch_currentcolorscheme.color6,
      "children":[
        {
          "name":      "rsds",
          "longname":  "Surface Downwelling Shortwave Radiation",
          "shortname": "SW Radiation Dn"
        },
        {
          "name":      "rsus",
          "longname":  "Surface Upwelling Shortwave Radiation",
          "shortname": "SW Radiation Up"
        },
        {
          "name":      "rlds",
          "longname":  "Surface Downwelling Longwave Radiation",
          "shortname": "LW Radiation Dn"
        },
        {
          "name":      "rlus",
          "longname":  "Surface Upwelling Longwave Radiation",
          "shortname": "LW Radiation Up"
        },
        {
          "name":      "rsdsdiff",
          "longname":  "Surface Diffuse Downwelling Shortwave Radiation",
          "shortname": "Diff. Radiation Dn",
          "tooltip":   "Diffuse SW radiation (so direct SW = rsds-rsdsdiff)"
        },
        {
          /* can we insert a blank line so as to create two groups within the temperature box */
        },
        {
          "name":      "clt",
          "longname":  "Total Cloud Fraction",
          "shortname": "Clouds"
        }
      ]
    },
    {
      "shortname":   "Pressure",
      "weathericon": "wi wi-barometer",
      "color":       esgfsearch_currentcolorscheme.color7,
      "children": [
        {
          "name":      "ps",
          "longname":  "Surface Air Pressur",
          "shortname": "Pressure",
          "tooltip":   "surface air pressure, as in observed station data"
        },
        {
          "name":      "psl",
          "longname":  "Sea-Level Air Pressure",
          "shortname": "SL Pressure",
          "tooltip":   "sea level air pressure"
        },
        {
          /* can we insert a blank line so as to create two groups within the temperature box */
        },
        {
          "name":      "pfull",
          "longname":  "Air Pressure",
          "shortname": "Pressure",
          "tooltip":   "air pressure at different model levels/altitudes"
        }
      ]
    },
    {
      "shortname":   "Evaporation",
      "weathericon": "wi wi-hot",
      "color":       esgfsearch_currentcolorscheme.color4,
      "children": [
        {
          "name":      "evspsbl",
          "longname":  "Actual Evaporation",
          "shortname": "Act. Evap."
        },
        {
          "name":      "evspsblpot",
          "longname":  "Potential Evaporation",
          "shortname": "Pot. Evap."
        },
        {
          /* can we insert a blank line so as to create two groups within the temperature box */
        },
        {
          "name":      "evspsblsoi",
          "longname":  "Bare Soil Evaporation",
          "shortname": "Soil Evap."
        },
        {
          "name":      "evspsblveg",
          "longname":  "Vegetation Canopy Evaporation",
          "shortname": "Canopy Evap."
        }
      ]
    }
  ]
};



var esgfsearch_pc_time_start_stop={
  "onlyquickselect":true
};

var esgfsearch_pc_bbox={
  "onlyquickselect":true
};

var esgfsearch_pc_query={
  "onlyquickselect":true
};