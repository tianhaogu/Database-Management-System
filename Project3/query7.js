// query7: Find the number of users born in each month using MapReduce

var num_month_mapper = function() {
    // Implement the map function
    emit(this.MOB, {"count_user": 1});
}

var num_month_reducer = function(key, values) {
    // Implement the reduce function
    ret_user_count = 0;
    for (var i = 0; i < values.length; i++) {
        ret_user_count += values[i]["count_user"];
    }
    var ret = {"count_user": ret_user_count};
    return ret;
}

var num_month_finalizer = function(key, reduceVal) {
    // We've implemented a simple forwarding finalize function. This implementation
    // is naive: it just forwards the reduceVal to the output collection.
    // Feel free to change it if needed.
    var ret = reduceVal.count_user;
    return ret;
}
