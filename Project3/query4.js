
// query 4: find user pairs (A,B) that meet the following constraints:
// i) user A is male and user B is female
// ii) their Year_Of_Birth difference is less than year_diff
// iii) user A and B are not friends
// iv) user A and B are from the same hometown city
// The following is the schema for output pairs:
// [
//      [user_id1, user_id2],
//      [user_id1, user_id3],
//      [user_id4, user_id2],
//      ...
//  ]
// "user_id" is the field from the users collection that you should use. 
// Do not use the "_id" field in the users collection.
  
function suggest_friends(year_diff, dbname) {
    db = db.getSiblingDB(dbname)
    var pairs = [];
    // TODO: implement suggest friends
    // Return an array of arrays.
    db.users.find({"gender": "male"}, {_id:0, user_id:1, YOB:1, gender:1, hometown:1, friends:1}).forEach(function(User1) {
        db.users.find({"gender": "female",
                       $and: [ {"YOB": {$gt:User1.YOB-year_diff}}, {"YOB": {$lt:User1.YOB+year_diff}} ],
                       "hometown.city": User1.hometown.city,
                       "user_id": {$nin: User1.friends}},
                       {_id:0, user_id:1, YOB:1, gender:1, hometown:1, friends:1}).forEach(function(User2) {
            if (User1.user_id > User2.user_id) {
                if (db.users.find({$and: [{"user_id": User1.user_id}, {"user_id": {$in: User2.friends}}]}, {_id:0, user_id:1, friends:1}).length() == 0) {
                    pairs.push([User1.user_id, User2.user_id]);
                }
            }
            else {
                pairs.push([User1.user_id, User2.user_id]);
            }
        })
    });
    return pairs;
}
