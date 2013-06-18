package com.cubeia.firebase.protocol;

import static com.cubeia.firebase.protocol.StyxJsonSerializerTest.SimpleObject.Status.BAD;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.cubeia.firebase.io.ObjectFactory;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxJsonSerializer;

public class StyxJsonSerializerTest extends TestCase {

	public void testSimpleSerialize() {
		StyxJsonSerializer ser = new StyxJsonSerializer(null);
		String json = ser.toJson(new SimpleObject());
		System.err.println(json);
		assertEquals("{\"name\":\"kalle\",\"id\":666,\"status\":" + SimpleObject.Status.BAD.ordinal() + ",\"classId\":1}", json);
	}
	
	public void testSimpleDeserialize() {
		StyxJsonSerializer ser = new StyxJsonSerializer(new Factory());
		SimpleObject o1 = new SimpleObject();
		String json = ser.toJson(o1);
		SimpleObject o2 = (SimpleObject) ser.fromJson(json);
		assertEquals(o1, o2);
	}
	
	public void testSimpleListDeserialize() {
		StyxJsonSerializer ser = new StyxJsonSerializer(new Factory());
		List<ProtocolObject> l1 = new ArrayList<ProtocolObject>();
		l1.add(new SimpleObject(1, "kalle", SimpleObject.Status.OK));
		l1.add(new SimpleObject(2, "olle", SimpleObject.Status.BAD));
		String json = ser.toJsonList(l1);
		// System.out.println(json);
		List<ProtocolObject> l2 = (List<ProtocolObject>) ser.fromJsonList(json);
		assertEquals(l1, l2);
	}
	
	public void testNestedProtocolObjects() {
		StyxJsonSerializer ser = new StyxJsonSerializer(new Factory());
		HardObject o1 = new HardObject(true);
		String json = ser.toJson(o1);
		System.out.println(json);
		HardObject o2 = (HardObject) ser.fromJson(json);
		assertEquals(o1, o2);		
	}
	
	private static class Factory implements ObjectFactory {
		
		@Override
		public ProtocolObject create(int classId) {
			if(classId == 1) {
				return new SimpleObject();
			}
			if(classId == 2) {
				return new HardObject();
			}		
			fail("unknown class id: " + classId);
			return null; // will never get here
		}
		
		@Override
		public int version() {
			return 1;
		}
	}
	
	public static enum Gender {
		MALE,
		FEMALE
	}
	
	public static class HardObject extends ProtocolObjectAdapter {

		private Gender gender = Gender.MALE;
		private List<SimpleObject> list = new ArrayList<SimpleObject>();
		
		public HardObject() { 
			this(false);
		}
		
		public HardObject(boolean populate) {
			if(populate) {
				list.add(new SimpleObject(1, "olle", SimpleObject.Status.OK));
				list.add(new SimpleObject(2, "kalle", SimpleObject.Status.BAD));
			}
		}
		
		public Gender getGender() {
			return gender;
		}
		
		public void setGender(Gender gender) {
			this.gender = gender;
		}
		
		public List<SimpleObject> getList() {
			return list;
		}
		
		public void setList(List<SimpleObject> list) {
			this.list = list;
		}
		
		@Override
		public int classId() {
			return 2;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((gender == null) ? 0 : gender.hashCode());
			result = prime * result + ((list == null) ? 0 : list.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			HardObject other = (HardObject) obj;
			if (gender != other.gender)
				return false;
			if (list == null) {
				if (other.list != null)
					return false;
			} else if (!list.equals(other.list))
				return false;
			return true;
		}
	}
	
	public static class SimpleObject extends ProtocolObjectAdapter {
	    
	    public enum Status { OK, BAD };

		private String name = "kalle";
		private int id = 666;
		private Status status = BAD;
		
		public SimpleObject() { }
		
		public SimpleObject(int i, String string, Status status) {
			this.name = string;
			this.id = i;
			this.status = status;
		}
		
		public int getId() {
			return id;
		}
		
		public void setId(int id) {
			this.id = id;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public Status getStatus() {
            return status;
        }
		
		public void setStatus(Status status) {
            this.status = status;
        }

		@Override
		public int classId() {
			return 1;
		}

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + id;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((status == null) ? 0 : status.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SimpleObject other = (SimpleObject) obj;
            if (id != other.id)
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (status != other.status)
                return false;
            return true;
        }

	}
}
