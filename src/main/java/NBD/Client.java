package NBD;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

@Entity
public class Client {
    @PartitionKey
    @Column(name = "clientId")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "age")
    private int age;

    public Client(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Client() {}


    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
