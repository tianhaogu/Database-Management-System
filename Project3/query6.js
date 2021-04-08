// query6 : Find the average user friend count
//
// Return a decimal as the average user friend count of all users
// in the users document.

function find_average_friendcount(dbname){
    db = db.getSiblingDB(dbname)
    // TODO: return a decimal number of average friend count
    var count_friends = 0;
    var count_users = 0;
    db.users.find({}, {_id:0, user_id:1, friends:1}).forEach(function(myUser) {
        count_friends += myUser.friends.length;
        count_users += 1;
    });
    return count_friends / count_users;
}
