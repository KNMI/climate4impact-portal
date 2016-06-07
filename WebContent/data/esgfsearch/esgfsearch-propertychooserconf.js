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
  "color6":"#c33c3c",
  "color4":"#dda606",
  "color5":"#AEB404",
  "color3":"#4CAF50",
  "color7":"#8258FA"
};

var esgfsearch_currentcolorscheme = esgfsearch_colorscheme_blueblackwhite;

var esgfsearch_pc_project={
   "tilewidth":"200px",
  "properties":[
    {
      "name":"CMIP5",
      "longname":"Coupled Model Intercomparison Project Phase 5<br/>Long term global climate change runs",
      "shortname":"CMIP5",
      "color":esgfsearch_currentcolorscheme.color1
    },{
      "name":"CORDEX",
      "longname":"Coordinated Regional Climate Downscaling Experiment<br/>Long term regional climate change runs",
      "shortname":"CORDEX",
      "color":esgfsearch_currentcolorscheme.color2
    },/*{
      "name":"specs",
      "longname":"Seasonal-to-decadal climate Prediction for the improvement of European Climate Services",
      "shortname":"SPECS",
      "color":esgfsearch_currentcolorscheme.color3
    },*/{
        "name":"clipc",
        "longname":"Climate Information Platform for Copernicus",
        "shortname":"CLIPC",
        "color":esgfsearch_currentcolorscheme.color4
      }
  ]
};

var esgfsearch_pc_time_frequency={
  "tilewidth":"170px",
  "properties":[
    {
      "name":"3hr",
      "longname":"Sampling frequency every 3 hours",
      "shortname":"3hr",
      "color":esgfsearch_currentcolorscheme.color1
    },{
      "name":"6hr",
      "longname":"Sampling frequency every 6 hours",
      "shortname":"6hr",
      "color":esgfsearch_currentcolorscheme.color2
    },{
      "name":"day",
      "longname":"Daily sampling frequency",
      "shortname":"daily",
      "color":esgfsearch_currentcolorscheme.color3
    },{
      "name":"mon",
      "longname":"Monthly sampling frequency",
      "shortname":"monthly",
      "color":esgfsearch_currentcolorscheme.color5
    },{
      "name":"year",
      "longname":"Yearly sampling frequency",
      "shortname":"yearly",
      "color":esgfsearch_currentcolorscheme.color6
    }
  ]
};


var esgfsearch_pc_experiments={
  "tilewidth":"300px",
  "properties":
  [
    {
      "longname": "Historical",
      "shortname": "Historical",
      "color":esgfsearch_currentcolorscheme.color2,
      "children": [
        {
          "name": "historical",
          "longname": "Historical",
          "shortname": "Historical"
        }
      ]
    },{
      "longname": "Representative concentration pathway",
      "shortname": "RCP",
      "color":esgfsearch_currentcolorscheme.color1,
      "children": [
        {
          "name": "rcp26",
          "longname": "Representative concentration pathway with radiative forcing of 2.6 W m-2",
          "shortname": "Radiative forcing of 2.6 W m-2"
        },{
          "name": "rcp45",
          "longname": "Representative concentration pathway with radiative forcing of 4.5 W m-2",
          "shortname": "Radiative forcing of 4.5 W m-2"
        },{
          "name": "rcp60",
          "longname": "Representative concentration pathway with radiative forcing of 6.0 W m-2",
          "shortname": "Radiative forcing of 6.0 W m-2"
        },{
          "name": "rcp85",
          "longname": "Representative concentration pathway with radiative forcing of 8.5 W m-2",
          "shortname": "Radiative forcing of 8.5 W m-2"
        }
      ]
    }
  ]
}



var esgfsearch_pc_variables={ 
  "properties":[

    {
      "longname": "Air Temperature",
      "shortname": "Temperature",
      "color":esgfsearch_currentcolorscheme.color1,
      "children": [
        {
          "name": "tas",
          "longname": "Near-Surface Air Temperature",
          "shortname": "Temperature"
        },{
          "name": "tasmin",
          "longname": "Daily Minimum Near-Surface Air Temperature",
          "shortname": "Min. Temperature"
        },
        {
          "name": "tasmax",
          "longname": "Daily Maximum Near-Surface Air Temperature",
          "shortname": "Max. Temperature"
        },
        {
          "name": "ta",
          "longname": "Air Temperature",
          "shortname": "Air Temperature"
        }
      ]
    },
    {
      "name": "pr",
      "longname": "Total Precipitation",
      "shortname": "Precipitation",
      "color":esgfsearch_currentcolorscheme.color2,
      "children": [ 
        {
          "name": "pr",
          "longname": "Total Precipitation",
          "shortname": "Rain"
        },
        {
          "name": "prc",
          "longname": "Convective Precipitation",
          "shortname": "Conv. Precip."
        },
        {
          "name": "prsn",
          "longname": "Snowfall flux",
          "shortname": "Snow"
        }
      ]
    },
    {
    
      "shortname": "Humidity",
      "color":esgfsearch_currentcolorscheme.color3,
      "children": [
        {
          "name": "huss",
          "longname": "Near-Surface Specific Humidity",
          "shortname": "Specific Humidity"
        },{
          "name": "hurs",
          "longname": "Near-Surface Relative Humidity",
          "shortname": "Rel. Humidity"
        },
        {
          "name": "hus",
          "longname": "Specific Humidity",
          "shortname": "Spec. Humidity"
        },
        {
          "name": "hur",
          "longname": "Relative Humidity",
          "shortname": "Rel. Humidity"
        },
        {
          "name": "rhs",
          "longname": "Daily Near-Surface Specific Humidity",
          "shortname": "Rel. Humidity"
        },
        {
          "name": "rhsmax",
          "longname": "Daily Maximum Near-Surface Relative Humidity",
          "shortname": "Max. Rel. Humidity"
        },
        {
          "name": "rhsmin",
          "longname": "Daily Minimium Near-Surface Relative Humidity",
          "shortname": "Min. Rel. Humidity"
        }
      ]
    },
    {
      "shortname": "Wind",
      "color":esgfsearch_currentcolorscheme.color5,
      "children": [
        {
          "name": "sfcWind",
          "longname": "Near-Surface Wind Speed",
          "shortname": "Wind"
        },{
          "name": "sfcWindmax",
          "longname": "Daily Maximum Near-Surface Wind Speed",
          "shortname": "Max. Wind"
        },
        {
          "name": "uas",
          "longname": "Eastward Near-Surface Wind",
          "shortname": "E. Wind"
        },
        {
          "name": "vas",
          "longname": "Northward Near-Surface Wind Speed",
          "shortname": "N. Wind"
        }
      ]
    },
    {
      "shortname": "Radiation",
      "color":esgfsearch_currentcolorscheme.color6,
      "children":[
        {
          "name": "rsds",
          "longname": "Surface Downwelling Shortwave Radiation",
          "shortname": "SW Radiation"
        },{
          "name": "rsus",
          "longname": "Surface Upwelling Shortwave Radiation",
          "shortname": "SW Up Radiation"
        },
        {
          "name": "rlds",
          "longname": "Surface Downwelling Longwave Radiation",
          "shortname": "LW Radiation"
        },
        {
          "name": "rlus",
          "longname": "Surface Upwelling Longwave Radiation",
          "shortname": "LW Up Radiation"
        },
        {
          "name": "rsdsdiff",
          "longname": "Surface Diffuse Downwelling Shortwave Radiation",
          "shortname": "Diff. Radiation"
        },
        {
          "name": "clt",
          "longname": "Total Cloud Fraction",
          "shortname": "Clouds"
        }
      ]
    },
    {
      "shortname": "Pressure",
      "color":esgfsearch_currentcolorscheme.color7,
      "children": [
        {
          "name": "ps",
          "longname": "Surface Air Pressur",
          "shortname": "Pressure"
        },{
          "name": "psl",
          "longname": "Sea-Level Air Pressure",
          "shortname": "SL Pressure"
        },
        {
          "name": "evspsbl",
          "longname": "Actual Evaporation",
          "shortname": "Evaporation"
        },
        {
          "name": "evspsblpot",
          "longname": "Potential Evaporation",
          "shortname": "Pot. Evaporation"
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