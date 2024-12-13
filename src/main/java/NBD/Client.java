package NBD;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;

import java.util.UUID;

@Entity
@CqlName("clients")
public class Client {
    @PartitionKey
    @CqlName("clientId")
    private UUID id;

    @CqlName("name")
    private String name;

    @CqlName("age")
    private int age;

    public Client(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Client(UUID id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public Client() {}


    public UUID getId() {
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
