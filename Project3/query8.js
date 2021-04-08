// query 8: Find the average user friend count for all users with the same hometown city
// using MapReduce
// Using the same terminology in query7, we are asking you to write the mapper,
// reducer and finalizer to find the average friend count for each hometown city.


var city_average_friendcount_mapper = function() {
    // implement the Map function of average friend count
    emit(this.hometown.city, {"friends_array": this.friends, "count_user": 1});
};

var city_average_friendcount_reducer = function(key, values) {
    // implement the reduce function of average friend count
    var ret_friends_concat = [];
    var ret_user_count = 0;
    for (var i = 0; i < values.length; i++) {
        ret_friends_concat = ret_friends_concat.concat(values[i]["friends_array"]);
        ret_user_count += values[i]["count_user"];
    }
    var ret = {"friends_array": ret_friends_concat, "count_user": ret_user_count};
    return ret;
};

var city_average_friendcount_finalizer = function(key, reduceVal) {
    // We've implemented a simple forwarding finalize function. This implementation
    // is naive: it just forwards the reduceVal to the output collection.
    // You may need to change it to pass the test case for this query
    var ret = reduceVal.friends_array.length / reduceVal.count_user;
    return ret;
}

