// query2 : unwind friends and create a collection called 'flat_users' 
// where each document in the collection has the following schema:
/*
{
  user_id:xxx
  friends:xxx
}
*/

function unwind_friends(dbname){
    db = db.getSiblingDB(dbname)
    // TODO: unwind friends
    db.users.aggregate([{$project: {_id: 0, user_id: 1, friends: 1}}, {$unwind: "$friends"}, {$out: "flat_users"}]);
    // returns nothing. It creates a collection instead as specified above.
}
