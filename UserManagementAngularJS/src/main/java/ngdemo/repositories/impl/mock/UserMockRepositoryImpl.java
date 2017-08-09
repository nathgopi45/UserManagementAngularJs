package ngdemo.repositories.impl.mock;

import java.util.ArrayList;
import java.util.List;

import ngdemo.domain.User;
import ngdemo.repositories.contract.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

@Component
public class UserMockRepositoryImpl extends GenericMockRepository<User> implements UserRepository {


	private static final Logger LOGGER = LoggerFactory.getLogger(UserMockRepositoryImpl.class);

	@Autowired
	private MongoTemplate mongoTemplate;



	private List<User> users = new ArrayList<>();

	


	public List<User> getAll() {
		return this.users;
	}

	@Override
	public User create(User user) {
		this.users.add(user);
		try {
			this.save(user);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
	}

	@Override
	public User update(User user) {
		try {
			user=	this.upsert(user);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
	}

	@Override
	public void remove(int id) {
		User byId = this.getById(id);
		this.users.remove(byId);
	}

	@Override
	public int getNumberOfUsers() {
		return this.users.size();
	}

	



	public void delete(final User User) throws Exception  {
		if (User != null) {			
			MongoClient m1 = new MongoClient();
			DB db = m1.getDB("test");
			DBCollection coll = db.getCollection("User");
			BasicDBObject b1 = new BasicDBObject(User.getFirstName(),
					User.getLastName());
			com.mongodb.WriteResult c1 = coll.remove(b1);
		} else {
			throw new Exception("invalid Key Value Exception");
		}
	}

	public void save(final User User) throws Exception  {
		if (User != null) {			
			MongoClient m1 = new MongoClient();
			DB db = m1.getDB("test");
			DBCollection coll = db.getCollection("User");
			BasicDBObject document = new BasicDBObject();
			document.put(User.getFirstName(),User.getLastName());
			coll.insert(document);

			LOGGER.info("Event stored successfully. document:{}", User);
		} else {
			throw new Exception("invalid Key Value Exception");
		}
	}  
	public void query(final User User) throws Exception  {
		MongoClient m1 = new MongoClient();
		DB db = m1.getDB("test");
		DBCollection coll = db.getCollection("User");
		DBCursor cur = coll.find();

		for (DBObject doc : cur) {
			String userName = (String) doc.get("id");
			System.out.println(userName);
		}
	}
	public User upsert(final User User) throws Exception {
		Query query = new Query(Criteria.where("_id").is(User.getFirstName()));
		User existingUser = mongoTemplate.findOne(query, User.class);
		if (existingUser != null) {
			DBObject dbDoc = new BasicDBObject();
			existingUser.setFirstName(User.getFirstName());
			mongoTemplate.getConverter().write(existingUser, dbDoc);
			Update update = fromDBObjectExcludeNullFields(dbDoc);
			mongoTemplate.upsert(query, update, User.class);
			LOGGER.info("User {} updated successfully", existingUser);
		} else {
			save(User);
			LOGGER.info("User {} inserted successfully", User);
		}
		return User;
	}

	public Update fromDBObjectExcludeNullFields(DBObject object) {
		Update update = new Update();
		for (String key : object.keySet()) {
			Object value = object.get(key);
			if (value != null) {
				update.set(key, value);
			}
		}
		return update;
	}

}
