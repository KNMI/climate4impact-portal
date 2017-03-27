var climate_indices_DEF = [
  {
    "cf_level": "CF-1.6", 
    "varname": "fd", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "count of days when daily minimum temperature is below 0 degC", 
      "long_name": "Frost days", 
      "short_name": "fd", 
      "definition": "Count when TN < 0\u00baC"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "0"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "1", 
      "long_name": "Number of Frost Days (Tmin < 0C)", 
      "standard_name": "number_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: minimum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tnlt2", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "count of days when daily minimum temperature is below plus 2 degC", 
      "long_name": "Frost days 2", 
      "short_name": "fd2", 
      "definition": "Count when TN < 2\u00baC"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "2"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "1", 
      "long_name": "Number of Frost Days (Tmin < +2C)", 
      "standard_name": "number_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: minimum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tnltm2", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "count of days when daily minimum temperature is below minus 2 degC", 
      "long_name": "Hard freeze", 
      "short_name": "fdm2", 
      "definition": "Count when TN < -2\u00baC"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "-2"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "1", 
      "long_name": "Number of Hard Freeze Days (Tmin < -2C)", 
      "standard_name": "number_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: minimum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tnltm20", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "count of days when daily minimum temperature is below minus 20 degC", 
      "long_name": "Very hard freeze", 
      "short_name": "fdm20", 
      "definition": "Count when TN < -20\u00baC"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "-20"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "1", 
      "long_name": "Number of Very Hard Freeze Days (Tmin < -20C)", 
      "standard_name": "number_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: minimum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "id", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "count of days when daily maximum temperature is below 0 degC", 
      "long_name": "Ice days", 
      "short_name": "id", 
      "definition": "Count when TX < 0\u00baC"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "0"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "1", 
      "long_name": "Number of Ice Days (Tmax < 0C)", 
      "standard_name": "number_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: maximum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "su", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "count of days when daily maximum temperature is above plus 25 degC", 
      "long_name": "Summer days", 
      "short_name": "su", 
      "definition": "Count when TX > 25\u00baC"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "25"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "1", 
      "long_name": "Number of Summer Days (Tmax > 25C)", 
      "standard_name": "number_of_days_with_air_temperature_above_threshold", 
      "cell_methods": "time: maximum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "txge30", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "count of days when daily maximum temperature is above or equal plus 30 degC", 
      "long_name": "Hot days", 
      "short_name": "su30", 
      "definition": "Count when TX >= 30\u00baC"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">=", 
        "value": "30"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "1", 
      "long_name": "Number of Hot Days (Tmax >= 30C)", 
      "standard_name": "number_of_days_with_air_temperature_above_or equal_threshold", 
      "cell_methods": "time: maximum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "txge35", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "count of days when daily maximum temperature is above or equal plus 35 degC", 
      "long_name": "Very hot days", 
      "short_name": "su35", 
      "definition": "Count when TX >= 35\u00baC"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">=", 
        "value": "35"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "1", 
      "long_name": "Number of Very Hot Days (Tmax >= 35C)", 
      "standard_name": "number_of_days_with_air_temperature_above_or equal_threshold", 
      "cell_methods": "time: maximum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tr", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "count of days when daily minimum temperature is above plus 20 degC", 
      "long_name": "Tropical nights", 
      "short_name": "tr", 
      "definition": "Count when TN > 20\u00baC"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "20"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "1", 
      "long_name": "Number of Tropical Nights (Tmin > 20C)", 
      "standard_name": "number_of_days_with_air_temperature_above_threshold", 
      "cell_methods": "time: minimum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tmge5", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "count of days when daily mean temperature is above plus 5 degC", 
      "long_name": "TM above 5C", 
      "short_name": "tm5a", 
      "definition": "Count when TM >= 5\u00baC"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "5"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "1", 
      "long_name": "Number of days with Tmean >= 5C", 
      "standard_name": "number_of_days_with_air_temperature_above_or_equal_threshold", 
      "cell_methods": "time: mean within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tmlt5", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "count of days when daily mean temperature is below plus 5 degC", 
      "long_name": "TM below 5C", 
      "short_name": "tm5b", 
      "definition": "Count when TM < 5\u00baC"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "5"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "1", 
      "long_name": "Number of days with Tmean < 5C", 
      "standard_name": "number_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: mean within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tmge10", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "count of days when daily mean temperature is above plus 10 degC", 
      "long_name": "TM above 10C", 
      "short_name": "tm10a", 
      "definition": "Count when TM >= 10\u00baC"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "10"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "1", 
      "long_name": "Number of days with Tmean >= 10C", 
      "standard_name": "number_of_days_with_air_temperature_above_or_equal_threshold", 
      "cell_methods": "time: mean within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tmlt10", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "count of days when daily mean temperature is below plus 10 degC", 
      "long_name": "TM below 10C", 
      "short_name": "tm10b", 
      "definition": "Count when TM < 10\u00baC"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "10"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "1", 
      "long_name": "Number of days with Tmean < 10C", 
      "standard_name": "number_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: mean within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tngt#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "count of days when daily minimum temperature is above # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Number of days with Tmin > #C", 
      "standard_name": "number_of_days_with_air_temperature_above_threshold", 
      "cell_methods": "time: minimum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tnlt#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "count of days when daily minimum temperature is below # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Number of days with Tmin < #C", 
      "standard_name": "number_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: minimum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tnge#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "count of days when daily minimum temperature is above or equal # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">=", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Number of days with Tmin >= #C", 
      "standard_name": "number_of_days_with_air_temperature_above_or equal_threshold", 
      "cell_methods": "time: minimum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tnle#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "count of days when daily minimum temperature is below or equal# degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<=", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Number of days with Tmin <= #C", 
      "standard_name": "number_of_days_with_air_temperature_below_or equal_threshold", 
      "cell_methods": "time: minimum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "txgt#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "count of days when daily maximum temperature is above # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Number of days with Tmax > #C", 
      "standard_name": "number_of_days_with_air_temperature_above_threshold", 
      "cell_methods": "time: maximum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "txlt#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "count of days when daily maximum temperature is below # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Number of days with Tmax < #C", 
      "standard_name": "number_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: maximum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "txge#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "count of days when daily maximum temperature is above or equal # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">=", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Number of days with Tmax >= #C", 
      "standard_name": "number_of_days_with_air_temperature_above_or equal_threshold", 
      "cell_methods": "time: maximum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "txle#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "count of days when daily maximum temperature is below or equal # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<=", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Number of days with Tmax <= #C", 
      "standard_name": "number_of_days_with_air_temperature_below_or equal_threshold", 
      "cell_methods": "time: maximum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tmgt#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "count of days when daily mean temperature is above # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Number of days with Tmean > #C", 
      "standard_name": "number_of_days_with_air_temperature_above_threshold", 
      "cell_methods": "time: mean within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tmlt#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "count of days when daily mean temperature is below # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Number of days with Tmean < #C", 
      "standard_name": "number_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: mean within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tmge#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "count of days when daily mean temperature is above or equal # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">=", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Number of days with Tmean >= #C", 
      "standard_name": "number_of_days_with_air_temperature_above_or equal_threshold", 
      "cell_methods": "time: mean within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tmle#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "count of days when daily mean temperature is below or equal# degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<=", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Number of days with Tmean <= #C", 
      "standard_name": "number_of_days_with_air_temperature_below_or equal_threshold", 
      "cell_methods": "time: mean within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "ctngt#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "count of days when daily minimum temperature is above # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "day", 
      "long_name": "Maximum number of consequtive days with Tmin > #C", 
      "standard_name": "spell_length_of_days_with_air_temperature_above_threshold", 
      "cell_methods": "time: minimum within days time: maximum over days", 
      "legend_units": "days", 
      "units": "day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "ctnlt#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "count of days when daily minimum temperature is below # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "day", 
      "long_name": "Maximum number of consequtive days with Tmin < #C", 
      "standard_name": "spell_length_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: minimum within days time: maximum over days", 
      "legend_units": "days", 
      "units": "day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "ctnge#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "count of days when daily minimum temperature is above or equal # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">=", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "day", 
      "long_name": "Maximum number of consequtive days with Tmin >= #C", 
      "standard_name": "spell_length_of_days_with_air_temperature_above_or equal_threshold", 
      "cell_methods": "time: minimum within days time: maximum over days", 
      "legend_units": "days", 
      "units": "day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "ctnle#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "count of days when daily minimum temperature is below or equal# degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<=", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "day", 
      "long_name": "Maximum number of consequtive days with Tmin <= #C", 
      "standard_name": "spell_length_of_days_with_air_temperature_below_or equal_threshold", 
      "cell_methods": "time: minimum within days time: maximum over days", 
      "legend_units": "days", 
      "units": "day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "ctxgt#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "count of days when daily maximum temperature is above # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "day", 
      "long_name": "Maximum number of consequtive days with Tmax > #C", 
      "standard_name": "spell_length_of_days_with_air_temperature_above_threshold", 
      "cell_methods": "time: maximum within days time: maximum over days", 
      "legend_units": "days", 
      "units": "day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "ctxlt#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "count of days when daily maximum temperature is below # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "day", 
      "long_name": "Maximum number of consequtive days with Tmax < #C", 
      "standard_name": "spell_length_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: maximum within days time: maximum over days", 
      "legend_units": "days", 
      "units": "day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "ctxge#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "count of days when daily maximum temperature is above or equal # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">=", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "day", 
      "long_name": "Maximum number of consequtive days with Tmax >= #C", 
      "standard_name": "spell_length_of_days_with_air_temperature_above_or equal_threshold", 
      "cell_methods": "time: maximum within days time: maximum over days", 
      "legend_units": "days", 
      "units": "day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "ctxle#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "count of days when daily maximum temperature is below or equal # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<=", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "day", 
      "long_name": "Maximum number of consequtive days with Tmax <= #C", 
      "standard_name": "spell_length_of_days_with_air_temperature_below_or equal_threshold", 
      "cell_methods": "time: maximum within days time: maximum over days", 
      "legend_units": "days", 
      "units": "day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "ctmgt#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "count of days when daily mean temperature is above # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "day", 
      "long_name": "Maximum number of consequtive days with Tmean > #C", 
      "standard_name": "spell_length_of_days_with_air_temperature_above_threshold", 
      "cell_methods": "time: mean within days time: maximum over days", 
      "legend_units": "days", 
      "units": "day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "ctmlt#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "count of days when daily mean temperature is below # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "day", 
      "long_name": "Maximum number of consequtive days with Tmean < #C", 
      "standard_name": "spell_length_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: mean within days time: maximum over days", 
      "legend_units": "days", 
      "units": "day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "ctmge#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "count of days when daily mean temperature is above or equal # degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">=", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "day", 
      "long_name": "Maximum number of consequtive days with Tmean >= #C", 
      "standard_name": "spell_length_of_days_with_air_temperature_above_or equal_threshold", 
      "cell_methods": "time: mean within days time: maximum over days", 
      "legend_units": "days", 
      "units": "day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "ctmle#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "count of days when daily mean temperature is below or equal# degC", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<=", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "day", 
      "long_name": "Maximum number of consequtive days with Tmean <= #C", 
      "standard_name": "spell_length_of_days_with_air_temperature_below_or equal_threshold", 
      "cell_methods": "time: mean within days time: maximum over days", 
      "legend_units": "days", 
      "units": "day"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "gsl", 
    "ready": "0", 
    "n_thresholds": "2", 
    "n_inputs": "2", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }, 
      {
        "standard_name": "latitude", 
        "cell_methods": "?????", 
        "known_variables": "latitude"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "Growing season length", 
      "short_name": "gsl", 
      "definition": "Annual (1st Jan to 31st Dec in NH, 1st July to 30st June in SH) count between first span of at least 6 days with TM>5\u00baC and first span after July 1 (January 1 in SH) of 6 days with TM<5C"
    }, 
    "threshold": [
      {
        "units": "day", 
        "varname": "threshold_days", 
        "standard_name": "??????", 
        "relop": ">", 
        "value": "6"
      }, 
      {
        "units": "degree_Celsius", 
        "varname": "threshold_temp", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "5"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "??????", 
      "long_name": "ETCCDI Growing Season Length (Tmean > 5C)", 
      "standard_name": "??????", 
      "cell_methods": "??????", 
      "legend_units": "days", 
      "units": "??????"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "gsstart", 
    "ready": "0", 
    "n_thresholds": "2", 
    "n_inputs": "2", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }, 
      {
        "standard_name": "latitude", 
        "cell_methods": "?????", 
        "known_variables": "latitude"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "day", 
        "varname": "threshold_days", 
        "standard_name": "??????", 
        "relop": ">", 
        "value": "6"
      }, 
      {
        "units": "degree_Celsius", 
        "varname": "threshold_temp", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "5"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "??????", 
      "long_name": "Start of ETCCDI Growing Season (6 days with Tmean > 5C)", 
      "standard_name": "??????", 
      "cell_methods": "??????", 
      "legend_units": "day number", 
      "units": "??????"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "gsend", 
    "ready": "0", 
    "n_thresholds": "2", 
    "n_inputs": "2", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }, 
      {
        "standard_name": "latitude", 
        "cell_methods": "?????", 
        "known_variables": "latitude"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "day", 
        "varname": "threshold_days", 
        "standard_name": "??????", 
        "relop": ">", 
        "value": "6"
      }, 
      {
        "units": "degree_Celsius", 
        "varname": "threshold_temp", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "5"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "??????", 
      "long_name": "End of ETCCDI Growing Season (6 days with Tmean < 5C)", 
      "standard_name": "??????", 
      "cell_methods": "??????", 
      "legend_units": "day number", 
      "units": "??????"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "gsgdd", 
    "ready": "0", 
    "n_thresholds": "2", 
    "n_inputs": "2", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }, 
      {
        "standard_name": "latitude", 
        "cell_methods": "?????", 
        "known_variables": "latitude"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "day", 
        "varname": "threshold_days", 
        "standard_name": "??????", 
        "relop": ">", 
        "value": "6"
      }, 
      {
        "units": "degree_Celsius", 
        "varname": "threshold_temp", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "5"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "??????", 
      "long_name": "Degree Days (Tmean >5C) during ETCCDI Growing Season", 
      "standard_name": "??????", 
      "cell_methods": "??????", 
      "legend_units": "degree_Celsius day", 
      "units": "??????"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "txx", 
    "ready": "1", 
    "n_thresholds": "0", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "maximum of daily maximum temperature", 
      "long_name": "Maximum daily maximum temperature", 
      "short_name": "txx", 
      "definition": "Maximum value of daily TX"
    }, 
    "threshold": {}, 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "Kelvin", 
      "long_name": "Maximum daily maximum temperature", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: maximum within days time: maximum over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tnx", 
    "ready": "1", 
    "n_thresholds": "0", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "maximum of daily minimum temperature", 
      "long_name": "Maximum daily minimum temperature", 
      "short_name": "tnx", 
      "definition": "Maximum value of daily TN"
    }, 
    "threshold": {}, 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "Kelvin", 
      "long_name": "Maximum daily minimum temperature", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: minimum within days time: maximum over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "txn", 
    "ready": "1", 
    "n_thresholds": "0", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "minimum of daily maximum temperature", 
      "long_name": "Minimum daily maximum temperature", 
      "short_name": "txn", 
      "definition": "Minimum value of daily TX"
    }, 
    "threshold": {}, 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "Kelvin", 
      "long_name": "Minimum daily maximum temperature", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: maximum within days time: minimum over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tnn", 
    "ready": "1", 
    "n_thresholds": "0", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "minimum of daily minimum temperature", 
      "long_name": "Minimum daily minimum temperature", 
      "short_name": "tnn", 
      "definition": "Minimum value of daily TN"
    }, 
    "threshold": {}, 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "Kelvin", 
      "long_name": "Minimum daily minimum temperature", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: minimum within days time: minimum over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "txmax", 
    "ready": "1", 
    "n_thresholds": "0", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "mean of daily maximum temperature", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": {}, 
    "output": {
      "reference": "_", 
      "canonical_units": "Kelvin", 
      "long_name": "Maximum daily maximum temperature", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: maximum within days time: maximum over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tnmax", 
    "ready": "1", 
    "n_thresholds": "0", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "mean of daily minimum temperature", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": {}, 
    "output": {
      "reference": "_", 
      "canonical_units": "Kelvin", 
      "long_name": "Maximum daily minimum temperature", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: minimum within days time: maximum over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "txmin", 
    "ready": "1", 
    "n_thresholds": "0", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "maximum of daily mean temperature", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": {}, 
    "output": {
      "reference": "_", 
      "canonical_units": "Kelvin", 
      "long_name": "Minimum daily maximum temperature", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: maximum within days time: minimum over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tnmin", 
    "ready": "1", 
    "n_thresholds": "0", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "minimum of daily mean temperature", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": {}, 
    "output": {
      "reference": "_", 
      "canonical_units": "Kelvin", 
      "long_name": "Minimum daily minimum temperature", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: minimum within days time: minimum over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "txmean", 
    "ready": "1", 
    "n_thresholds": "0", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "mean of daily maximum temperature", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": {}, 
    "output": {
      "reference": "_", 
      "canonical_units": "Kelvin", 
      "long_name": "Mean daily maximum temperature", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: maximum within days time: mean over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tnmean", 
    "ready": "1", 
    "n_thresholds": "0", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "mean of daily minimum temperature", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": {}, 
    "output": {
      "reference": "_", 
      "canonical_units": "Kelvin", 
      "long_name": "Mean daily minimum temperature", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: minimum within days time: mean over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tmmax", 
    "ready": "1", 
    "n_thresholds": "0", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "maximum of daily mean temperature", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": {}, 
    "output": {
      "reference": "_", 
      "canonical_units": "Kelvin", 
      "long_name": "Maximum daily mean temperature", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: mean within days time: maximum over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tmmin", 
    "ready": "1", 
    "n_thresholds": "0", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "minimum of daily mean temperature", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": {}, 
    "output": {
      "reference": "_", 
      "canonical_units": "Kelvin", 
      "long_name": "Minimum daily mean temperature", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: mean within days time: maximum over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "tmmean", 
    "ready": "1", 
    "n_thresholds": "0", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "mean of daily mean temperature", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": {}, 
    "output": {
      "reference": "_", 
      "canonical_units": "Kelvin", 
      "long_name": "Mean daily mean temperature", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: mean within days time: mean over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "wsdi", 
    "ready": "0", 
    "n_thresholds": "2", 
    "n_inputs": "1", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "Warm spell duration index", 
      "short_name": "wsdi", 
      "definition": "Annual count of days with at least 6 consecutive days when TX>90th percentile"
    }, 
    "threshold": [
      {
        "units": "day", 
        "varname": "threshold_days", 
        "standard_name": "??????", 
        "relop": ">", 
        "value": "6"
      }, 
      {
        "units": "1 (=quantile (--> # degree_Celsius) )", 
        "varname": "threshold_temp", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "0.90"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "1", 
      "long_name": "Warm Spell Duration Index, count of days with at least 6 consecutive days when Tmax > 90th percentile", 
      "standard_name": "??????", 
      "cell_methods": "??????", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "wsdi#", 
    "ready": "0", 
    "n_thresholds": "2", 
    "n_inputs": "1", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "User-defined WSDI", 
      "short_name": "wsdin", 
      "definition": "Annual count of days with at least n consecutive days when TX>90th percentile where n>= 2 (and  max 10)"
    }, 
    "threshold": [
      {
        "units": "day", 
        "varname": "threshold_days", 
        "standard_name": "??????", 
        "relop": ">", 
        "value": "#"
      }, 
      {
        "units": "1 (=quantile (--> # degree_Celsius) )", 
        "varname": "threshold_temp", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "0.90"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "1", 
      "long_name": "User-defined Warm Spell Duration Index, count of days with at least # consecutive days when Tmax > 90th percentile", 
      "standard_name": "??????", 
      "cell_methods": "??????", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "csdi", 
    "ready": "0", 
    "n_thresholds": "2", 
    "n_inputs": "1", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "Cold spell duration index", 
      "short_name": "csdi", 
      "definition": "Annual count of days with at least 6 consecutive days when TN<10th percentile"
    }, 
    "threshold": [
      {
        "units": "day", 
        "varname": "threshold_days", 
        "standard_name": "??????", 
        "relop": ">", 
        "value": "6"
      }, 
      {
        "units": "1 (=quantile (--> # degree_Celsius) )", 
        "varname": "threshold_temp", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "0.90"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "1", 
      "long_name": "Cold Spell Duration Index, count of days with at least 6 consecutive days when Tmin < 10th percentile", 
      "standard_name": "??????", 
      "cell_methods": "??????", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "csdi#", 
    "ready": "0", 
    "n_thresholds": "2", 
    "n_inputs": "1", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "User-defined CSDI", 
      "short_name": "csdin", 
      "definition": "Annual count of days with at least n consecutive days when TN<10th percentile where n>= 2 (and  max 10)"
    }, 
    "threshold": [
      {
        "units": "day", 
        "varname": "threshold_days", 
        "standard_name": "??????", 
        "relop": ">", 
        "value": "#"
      }, 
      {
        "units": "1 (=quantile (--> # degree_Celsius) )", 
        "varname": "threshold_temp", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "0.90"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "1", 
      "long_name": "User-defined Cold Spell Duration Index, count of days with at least # consecutive days when Tmin < 10th percentile", 
      "standard_name": "??????", 
      "cell_methods": "??????", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "tn10p", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "WMO No.1500: Cold nights (count of days)", 
      "short_name": "tn10p", 
      "definition": "Number of days when TN < 10th percentile"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile (--> # degree_Celsius) )", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "0.10"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "1", 
      "long_name": "Percentage of days when Tmin < 10th percentile", 
      "standard_name": "fraction_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: minimum within days time: maximum over days", 
      "legend_units": "%", 
      "units": "%"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "tx10p", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "WMO No.1500: Cold day-times (count of days)", 
      "short_name": "tx10p", 
      "definition": "Number of days when TX < 10th percentile"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile (--> # degree_Celsius) )", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "0.10"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "1", 
      "long_name": "Percentage of days when Tmax < 10th percentile", 
      "standard_name": "fraction_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: maximum within days time: minimum over days", 
      "legend_units": "%", 
      "units": "%"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "tn90p", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "WMO No.1500: Warm nights (count of days)", 
      "short_name": "tn90p", 
      "definition": "Number of days when TN > 90th percentile"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile (--> # degree_Celsius) )", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "0.90"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "1", 
      "long_name": "Percentage of days when Tmin > 90th percentile", 
      "standard_name": "fraction_of_days_with_air_temperature_above_threshold", 
      "cell_methods": "time: minimum within days time: minimum over days", 
      "legend_units": "%", 
      "units": "%"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "tx90p", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "WMO No.1500: Warm day-times (count of days)", 
      "short_name": "tx90p", 
      "definition": "Number of days when TX > 90th percentile"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile (--> # degree_Celsius) )", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "0.90"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "1", 
      "long_name": "Percentage of days when Tmax > 90th percentile", 
      "standard_name": "fraction_of_days_with_air_temperature_above_threshold", 
      "cell_methods": "time: maximum within days time: maximum over days", 
      "legend_units": "%", 
      "units": "%"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "txgt#p", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile (--> # degree_Celsius) )", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Percentage of days when Tmax > #th percentile", 
      "standard_name": "fraction_of_days_with_air_temperature_above_threshold", 
      "cell_methods": "time: maximum within days time: maximum over days", 
      "legend_units": "%", 
      "units": "%"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "tngt#p", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile (--> # degree_Celsius) )", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Percentage of days when Tmin > #th percentile", 
      "standard_name": "fraction_of_days_with_air_temperature_above_threshold", 
      "cell_methods": "time: minimum within days time: maximum over days", 
      "legend_units": "%", 
      "units": "%"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "tmgt#p", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile (--> # degree_Celsius) )", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Percentage of days when Tmean > #th percentile", 
      "standard_name": "fraction_of_days_with_air_temperature_above_threshold", 
      "cell_methods": "time: mean within days time: maximum over days", 
      "legend_units": "%", 
      "units": "%"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "txlt#p", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile (--> # degree_Celsius) )", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Percentage of days when Tmax < #th percentile", 
      "standard_name": "fraction_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: maximum within days time: maximum over days", 
      "legend_units": "%", 
      "units": "%"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "tnlt#p", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile (--> # degree_Celsius) )", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Percentage of days when Tmin < #th percentile", 
      "standard_name": "fraction_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: minimum within days time: maximum over days", 
      "legend_units": "%", 
      "units": "%"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "tmlt#p", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile (--> # degree_Celsius) )", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Percentage of days when Tmean < #th percentile", 
      "standard_name": "fraction_of_days_with_air_temperature_below_threshold", 
      "cell_methods": "time: mean within days time: maximum over days", 
      "legend_units": "%", 
      "units": "%"
    }
  }, 
  {
    "cf_level": "CF-1.7 (draft)", 
    "varname": "dtr", 
    "ready": "1", 
    "n_thresholds": "0", 
    "n_inputs": "2", 
    "freq": "mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }, 
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "mean of daily temperature range", 
      "long_name": "Daily temperature range", 
      "short_name": "dtr", 
      "definition": "Monthly mean difference between TX and TN"
    }, 
    "threshold": {}, 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "Kelvin", 
      "long_name": "Mean Diurnal Temperature Range", 
      "standard_name": "air_temperature", 
      "cell_methods": "time range within days time: mean over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "tx95t", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "95th percentile of daily maximum temperature", 
      "long_name": "Very warm day threshold", 
      "short_name": "tx95t", 
      "definition": "Value of 95th percentile of TX"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile)", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "_", 
        "value": "0.95"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "Kelvin", 
      "long_name": "Very Warm Days threshold (95th percentile of Tmax)", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: maximum within days time: quantile over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "tx#pctl", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: maximum within days", 
        "known_variables": "tasmax"
      }
    ], 
    "et": {
      "comment": "95th percentile of daily maximum temperature", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile)", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "_", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "Kelvin", 
      "long_name": "#th percentile of Tmax", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: maximum within days time: quantile over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "tn#pctl", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: minimum within days", 
        "known_variables": "tasmin"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile)", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "_", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "Kelvin", 
      "long_name": "#th percentile of Tmin", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: minimum within days time: quantile over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "NOT", 
    "varname": "tm#pctl", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile)", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "_", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "Kelvin", 
      "long_name": "#th percentile of Tmean", 
      "standard_name": "air_temperature", 
      "cell_methods": "time: mean within days time: quantile over days", 
      "legend_units": "degree_Celsius", 
      "units": "degree_Celsius"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "hddheat#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "Heating degree days", 
      "short_name": "hddheat", 
      "definition": "Sum of Tb- TM (where Tb is a user- defined location-specific base temperature and TM < Tb)"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "# (=Tb)"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "Kelvin second", 
      "long_name": "Heating Degree Days (Tmean < #C)", 
      "standard_name": "integral_of_air_temperature_excess_wrt_time", 
      "cell_methods": "time: mean within days time: sum over days", 
      "legend_units": "degree-days", 
      "units": "degree_Celsius day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "ddgt#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "Kelvin second", 
      "long_name": "Degree Days (Tmean > #C)", 
      "standard_name": "integral_of_air_temperature_excess_wrt_time", 
      "cell_methods": "time: mean within days time: sum over days", 
      "legend_units": "degree-days", 
      "units": "degree_Celsius day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "cddcold#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "Cooling degree days", 
      "short_name": "cddcold", 
      "definition": "Sum of TM - Tb (where Tb is a user- defined location-specific base temperature and TM > Tb)"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "# (=Tb)"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "Kelvin second", 
      "long_name": "Cooling Degree Days (Tmean > #C)", 
      "standard_name": "integral_of_air_temperature_deficit_wrt_time", 
      "cell_methods": "time: mean within days time: sum over days", 
      "legend_units": "degree-days", 
      "units": "degree_Celsius day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "ddlt#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": "<", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "Kelvin second", 
      "long_name": "Degree Days (Tmean < #C)", 
      "standard_name": "integral_of_air_temperature_deficit_wrt_time", 
      "cell_methods": "time: mean within days time: sum over days", 
      "legend_units": "degree-days", 
      "units": "degree_Celsius day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "gddgrow#", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann", 
    "input": [
      {
        "standard_name": "air_temperature", 
        "cell_methods": "time: mean within days", 
        "known_variables": "tas"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "Growing degree days", 
      "short_name": "gddgrow", 
      "definition": "Annual sum of TM - Tb (where Tb is a user- defined location-specific base temperature and TM >Tb)"
    }, 
    "threshold": [
      {
        "units": "degree_Celsius", 
        "varname": "threshold", 
        "standard_name": "air_temperature", 
        "relop": ">", 
        "value": "# (=Tb)"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "Kelvin second", 
      "long_name": "Annual Growing Degree Days (Tmean > 5C)", 
      "standard_name": "integral_of_air_temperature_excess_wrt_time", 
      "cell_methods": "time: mean within days time: sum over days", 
      "legend_units": "degree-days", 
      "units": "degree_Celsius day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "r10mm", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "lwe_thickness_of_precipitation_amount", 
        "cell_methods": "time: sum within days", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "count of days when daily total precipitation is above 10 mm", 
      "long_name": "Number of heavy precipitation days", 
      "short_name": "r10mm", 
      "definition": "Count of days when P>=10mm"
    }, 
    "threshold": [
      {
        "units": "mm", 
        "varname": "threshold", 
        "standard_name": "lwe_thickness_of_precipitation_amount", 
        "relop": ">=", 
        "value": "10"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "1", 
      "long_name": "Number of heavy precipitation days (Precip >=10mm)", 
      "standard_name": "number_of_days_with_lwe_thickness_of_precipitation_amount_above_or_equal_threshold", 
      "cell_methods": "time: sum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "r20mm", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "lwe_thickness_of_precipitation_amount", 
        "cell_methods": "time: sum within days", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "count of days when daily total precipitation is above 20 mm", 
      "long_name": "Number of very heavy precipitation days", 
      "short_name": "r20mm", 
      "definition": "Count of days when P>=20mm"
    }, 
    "threshold": [
      {
        "units": "mm", 
        "varname": "threshold", 
        "standard_name": "lwe_thickness_of_precipitation_amount", 
        "relop": ">=", 
        "value": "20"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "1", 
      "long_name": "Number of very heavy precipitation days (Precip >= 20mm)", 
      "standard_name": "number_of_days_with_lwe_thickness_of_precipitation_amount_above_or_equal_threshold", 
      "cell_methods": "time: sum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "r#mm", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "lwe_thickness_of_precipitation_amount", 
        "cell_methods": "time: sum within days", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "count of days when daily total precipitation is above X mm", 
      "long_name": "Number of days above a user-defined threshold", 
      "short_name": "rnnmm", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "mm", 
        "varname": "threshold", 
        "standard_name": "lwe_thickness_of_precipitation_amount", 
        "relop": ">=", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "1", 
      "long_name": "Number of days with daily Precip >= #mm)", 
      "standard_name": "number_of_days_with_lwe_thickness_of_precipitation_amount_above_or_equal_threshold", 
      "cell_methods": "time: sum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "wetdays", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "lwe_thickness_of_precipitation_amount", 
        "cell_methods": "time: sum within days", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "mm", 
        "varname": "threshold", 
        "standard_name": "lwe_thickness_of_precipitation_amount", 
        "relop": ">=", 
        "value": "1"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "Number of Wet Days (precip >= 1 mm)", 
      "standard_name": "number_of_days_with_lwe_thickness_of_precipitation_amount_above_or_equal_threshold", 
      "cell_methods": "time: sum within days time: sum over days", 
      "legend_units": "days", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "cdd", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "lwe_thickness_of_precipitation_amount", 
        "cell_methods": "time: sum within days", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "maximum consecutive days when daily total precipitation is below 1 mm", 
      "long_name": "Consecutive dry days", 
      "short_name": "cdd", 
      "definition": "Maximum number of consecutive days with P<1mm"
    }, 
    "threshold": [
      {
        "units": "mm", 
        "varname": "threshold", 
        "standard_name": "lwe_thickness_of_precipitation_amount", 
        "relop": "<", 
        "value": "1"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "day", 
      "long_name": "Maximum consecutive dry days (Precip < 1mm)", 
      "standard_name": "spell_length_of_days_with_lwe_thickness_of_precipitation_amount_below_threshold", 
      "cell_methods": "time: sum within days time: sum over days", 
      "legend_units": "days", 
      "units": "day"
    }
  }, 
  {
    "cf_level": "CF-1.6", 
    "varname": "cwd", 
    "ready": "1", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "lwe_thickness_of_precipitation_amount", 
        "cell_methods": "time: sum within days", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "maximum consecutive days when daily total precipitation is at least 1 mm", 
      "long_name": "Consecutive wet days", 
      "short_name": "cwd", 
      "definition": "Maximum number of consecutive days with P>=1mm"
    }, 
    "threshold": [
      {
        "units": "mm", 
        "varname": "threshold", 
        "standard_name": "lwe_thickness_of_precipitation_amount", 
        "relop": ">=", 
        "value": "1"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "day", 
      "long_name": "Maximum consecutive wet days (Precip >= 1mm)", 
      "standard_name": "spell_length_of_days_with_lwe_thickness_of_precipitation_amount_above_or_equal_threshold", 
      "cell_methods": "time: sum within days time: sum over days", 
      "legend_units": "days", 
      "units": "day"
    }
  }, 
  {
    "cf_level": "", 
    "varname": "??????", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "??????", 
        "cell_methods": "??????", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "sum of total daily precipitation during days having at least 1 mm", 
      "long_name": "Total wet- day precipitation", 
      "short_name": "prcptot", 
      "definition": "PRCP from wet days (P>=1mm)"
    }, 
    "threshold": [
      {
        "units": "mm", 
        "varname": "??????", 
        "standard_name": "??????", 
        "relop": ">", 
        "value": "1"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "meter", 
      "long_name": "Total precipitation during Wet Days", 
      "standard_name": "lwe_thickness_of_precipitation_amount", 
      "cell_methods": "??????", 
      "legend_units": "mm", 
      "units": "mm"
    }
  }, 
  {
    "cf_level": "", 
    "varname": "sdii", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "??????", 
        "cell_methods": "??????", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "mean daily total precipitation during days having at least 1 mm", 
      "long_name": "Simple precipitation intensity index", 
      "short_name": "sdii", 
      "definition": "PRCPTOT / Nwetdays"
    }, 
    "threshold": [
      {
        "units": "mm", 
        "varname": "??????", 
        "standard_name": "??????", 
        "relop": ">=", 
        "value": "1"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "meter", 
      "long_name": "Average precipitation during Wet Days (SDII)", 
      "standard_name": "lwe_thickness_of_precipitation_amount", 
      "cell_methods": "??????", 
      "legend_units": "mm", 
      "units": "mm"
    }
  }, 
  {
    "cf_level": "", 
    "varname": "r95ptot", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "??????", 
        "cell_methods": "??????", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "Contribution from very wet days", 
      "short_name": "r95ptot", 
      "definition": "Total precipitation amount when RR > 95th pctl on wet days"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile (--> # mm) )", 
        "varname": "??????", 
        "standard_name": "??????", 
        "relop": ">", 
        "value": "0.95"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "meter", 
      "long_name": "Total precipitation amount when Precip > 95th pctl on Wet Days", 
      "standard_name": "??????", 
      "cell_methods": "??????", 
      "legend_units": "mm", 
      "units": "mm"
    }
  }, 
  {
    "cf_level": "", 
    "varname": "r99ptot", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "??????", 
        "cell_methods": "??????", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "Contribution from extremely wet days", 
      "short_name": "r99ptot", 
      "definition": "Total precipitation amount when RR > 99th pctl on wet days"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile (--> # mm) )", 
        "varname": "??????", 
        "standard_name": "??????", 
        "relop": ">", 
        "value": "0.99"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "meter", 
      "long_name": "Total precipitation amount when Precip > 99th pctl on Wet Days", 
      "standard_name": "??????", 
      "cell_methods": "??????", 
      "legend_units": "mm", 
      "units": "mm"
    }
  }, 
  {
    "cf_level": "", 
    "varname": "r#ptot", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "??????", 
        "cell_methods": "??????", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile (--> # mm) )", 
        "varname": "??????", 
        "standard_name": "??????", 
        "relop": ">", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "meter", 
      "long_name": "Total precipitation amount when Precip > #th pctl on Wet Days", 
      "standard_name": "??????", 
      "cell_methods": "??????", 
      "legend_units": "mm", 
      "units": "mm"
    }
  }, 
  {
    "cf_level": "", 
    "varname": "r95p", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "??????", 
        "cell_methods": "??????", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "Contribution from very wet days", 
      "short_name": "r95p", 
      "definition": "Annual total precipitation amount when RR > 95th pctl on wet days"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile (--> # mm) )", 
        "varname": "??????", 
        "standard_name": "??????", 
        "relop": ">", 
        "value": "0.95"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "1", 
      "long_name": "??????", 
      "standard_name": "??????", 
      "cell_methods": "??????", 
      "legend_units": "%", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "", 
    "varname": "r99p", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "??????", 
        "cell_methods": "??????", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "Contribution from extremely wet days", 
      "short_name": "r99p", 
      "definition": "Annual percentage of P>99th percentile /  PRCPTOT"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile (--> # mm) )", 
        "varname": "??????", 
        "standard_name": "??????", 
        "relop": ">", 
        "value": "0.99"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "1", 
      "long_name": "??????", 
      "standard_name": "??????", 
      "cell_methods": "??????", 
      "legend_units": "%", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "", 
    "varname": "r#pctl", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "??????", 
        "cell_methods": "??????", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "_", 
      "short_name": "_", 
      "definition": "_"
    }, 
    "threshold": [
      {
        "units": "1 (=quantile (--> # mm) )", 
        "varname": "??????", 
        "standard_name": "??????", 
        "relop": ">", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "_", 
      "canonical_units": "1", 
      "long_name": "??????", 
      "standard_name": "??????", 
      "cell_methods": "??????", 
      "legend_units": "%", 
      "units": "1"
    }
  }, 
  {
    "cf_level": "", 
    "varname": "rx1day", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "??????", 
        "cell_methods": "??????", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "Monthly maximum 1-day precipitation", 
      "short_name": "rx1day", 
      "definition": "Maximum one-day precipitation"
    }, 
    "threshold": [
      {
        "units": "day", 
        "varname": "??????", 
        "standard_name": "??????", 
        "relop": "=", 
        "value": "1"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "meter", 
      "long_name": "Maximum 1-day precipitation", 
      "standard_name": "lwe_thickness_of_precipitation_amount", 
      "cell_methods": "time: sum within days time: maximum over days", 
      "legend_units": "mm", 
      "units": "mm"
    }
  }, 
  {
    "cf_level": "", 
    "varname": "rx5day", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "??????", 
        "cell_methods": "??????", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "Monthly maximum 5-day precipitation", 
      "short_name": "rx5day", 
      "definition": "Maximum consecutive five-day precipitation"
    }, 
    "threshold": [
      {
        "units": "day", 
        "varname": "??????", 
        "standard_name": "??????", 
        "relop": "=", 
        "value": "5"
      }
    ], 
    "output": {
      "reference": "ETCCDI", 
      "canonical_units": "meter", 
      "long_name": "Maximum 5-day precipitation", 
      "standard_name": "lwe_thickness_of_precipitation_amount", 
      "cell_methods": "time: sum within days time: maximum over days", 
      "legend_units": "mm", 
      "units": "mm"
    }
  }, 
  {
    "cf_level": "", 
    "varname": "rx#day", 
    "ready": "0", 
    "n_thresholds": "1", 
    "n_inputs": "1", 
    "freq": "ann/sea/mon", 
    "input": [
      {
        "standard_name": "??????", 
        "cell_methods": "??????", 
        "known_variables": "pr (transformed)"
      }
    ], 
    "et": {
      "comment": "_", 
      "long_name": "User-defined consecutive days precipitation amount", 
      "short_name": "rxnday", 
      "definition": "Maximum consecutive n-day precipitation"
    }, 
    "threshold": [
      {
        "units": "day", 
        "varname": "??????", 
        "standard_name": "??????", 
        "relop": "=", 
        "value": "#"
      }
    ], 
    "output": {
      "reference": "ET-SCI", 
      "canonical_units": "meter", 
      "long_name": "Maximum #-day precipitation", 
      "standard_name": "lwe_thickness_of_precipitation_amount", 
      "cell_methods": "time: sum within days time: maximum over days", 
      "legend_units": "mm", 
      "units": "mm"
    }
  }
]
