// Find the oldest friend for each user who has a friend. 
// For simplicity, use only year of birth to determine age. If there is a tie, use the friend with the smallest user_id
// Return a javascript object : the keys should be user_ids and the value for each user_id is their oldest friend's user_id
// You may find query 2 and query 3 helpful. You can create selections if you want. Do not modify the users collection.
//
//You should return something like this:(order does not matter)
//{user1:oldestFriend1, user2:oldestFriend2, user3:oldestFriend3,...}

function oldest_friend(dbname) {
    db = db.getSiblingDB(dbname)
    var results = {};
    // TODO: implement oldest friends
    // return an javascript object described above
    db.users.aggregate([{$project: {_id: 0, user_id: 1, friends: 1}}, {$unwind: "$friends"}, {$out: "flat_users"}]);
    db.flat_users.find().forEach(function(User) { //reverse the {user_id, friends} to {friends, user_id} (like a union)
        db.reverse_flat_users.insertOne({"user_id": User.friends, "friends": User.user_id});
    });
    var user_yob = {};
    db.users.find({}, {_id:0, user_id:1, YOB:1}).forEach(function(User) { //in the form {user_id, YOB}
        user_yob[User.user_id] = User.YOB;
    });
    // create the table of form {user_id, [friend1, friend2, ......]}
    db.flat_users.aggregate([{$group: {_id: "$user_id", "friend_array": {$push: "$friends"}}}, {$out: "flat_pair_list"}]);
    db.reverse_flat_users.aggregate([{$group: {_id: "$user_id", "friend_array": {$push: "$friends"}}}, {$out: "reverse_flat_pair_list"}]);
    var max_friend_yob_order = 1000000;
    var max_friend_id_order = -1;
    var user_id_list_order = [];
    db.flat_pair_list.find().forEach(function(User) { //find the oldest friend of each user
        user_id_list_order.push(User._id);
        var friend_list = User.friend_array;
        var list_length = friend_list.length;
        for (var i = 0; i < list_length; i++) { //loop over the friend array in the positive {user_id, [friends]} object
            if (user_yob[friend_list[i]] <= max_friend_yob_order) {
                if (user_yob[friend_list[i]] < max_friend_yob_order) {
                    max_friend_yob_order = user_yob[friend_list[i]];
                    max_friend_id_order = friend_list[i];
                }
                else { //same YOB
                    if (friend_list[i] < max_friend_id_order) {
                        max_friend_yob_order = user_yob[friend_list[i]];
                        max_friend_id_order = friend_list[i];
                    }
                }
            }
        }
        var lst_find = db.reverse_flat_pair_list.find({_id: User._id});
        if (lst_find.count() != 0) {
            var thisUser = lst_find.next(); //lst_find.forEach(function(thisUser) {
            var friend_list_reverse = thisUser.friend_array;
            var list_length_reverse = friend_list_reverse.length;
            for (var i = 0; i < list_length_reverse; i++) { //loop over the friend array in the positive {user_id, [friends]} object
                if (user_yob[friend_list_reverse[i]] <= max_friend_yob_order) {
                    if (user_yob[friend_list_reverse[i]] < max_friend_yob_order) {
                        max_friend_yob_order = user_yob[friend_list_reverse[i]];
                        max_friend_id_order = friend_list_reverse[i];
                    }
                    else { //same YOB
                        if (friend_list_reverse[i] < max_friend_id_order) {
                            max_friend_yob_order = user_yob[friend_list_reverse[i]];
                            max_friend_id_order = friend_list_reverse[i];
                        }
                    }
                }
            }
        }
        results[User._id] = max_friend_id_order;
        max_friend_yob_order = 1000000;
        max_friend_id_order = -1;
    });

    db.reverse_flat_pair_list.find({"_id": {$nin: user_id_list_order}}).forEach(function(remainUser) {
        var friend_list_remain = remainUser.friend_array;
        var list_length_remain = friend_list_remain.length;
        for (var i = 0; i < list_length_remain; i++) {
            if (user_yob[friend_list_remain[i]] <= max_friend_yob_order) {
                if (user_yob[friend_list_remain[i]] < max_friend_yob_order) {
                    max_friend_yob_order = user_yob[friend_list_remain[i]];
                    max_friend_id_order = friend_list_remain[i];
                }
                else {
                    if (friend_list_remain[i] < max_friend_id_order) {
                        max_friend_yob_order = user_yob[friend_list_remain[i]];
                        max_friend_id_order = friend_list_remain[i];
                    }
                }
            }
        }
        results[remainUser._id] = max_friend_id_order;
        max_friend_yob_order = 1000000;
        max_friend_id_order = -1;
    });
    //for (var i = 0; i < 800; i++) {
    //    print(i, "    ", results[i], "    ", user_yob[results[i]]);
    //}
    db.reverse_flat_users.drop();
    return results;
}