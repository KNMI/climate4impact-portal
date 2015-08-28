var esgfsearch_pc_project=[
  {
    "name":"CMIP5",
    "longname":"Coupled Model Intercomparison Project Phase 5",
    "shortname":"CMIP5",
    "color":"#00a8ec"
  },{
    "name":"CORDEX",
    "longname":"Coordinated Regional Climate Downscaling Experiment",
    "shortname":"CORDEX",
    "color":"#f58d00"
  },{
    "name":"SPECS",
    "longname":"Seasonal-to-decadal climate Prediction for the improvement of European Climate Services",
    "shortname":"SPECS",
    "color":"#d32c2c"
  }
];

var esgfsearch_pc_variables=[
  {
    "longname": "Air Temperature",
    "shortname": "Temperature",
    "color":"#00a8ec",
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
    "color":"#f58d00",
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
    "color":"#d32c2c",
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
    "color":"#6b6b6b",
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
    "color":"#ffc808",
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
    "color":"#640f6c",
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
];
