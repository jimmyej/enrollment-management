package com.mitocode.repositories;

import com.mitocode.documents.Role;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends CrudRepository<Role, String>{

}
