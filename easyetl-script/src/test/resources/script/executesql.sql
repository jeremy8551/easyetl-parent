


declare global test0001  catalog configuration use driver $databaseDriverName url "${databaseUrl}" username ${username} password $password admin ${username} adminPw $password
db connect to test0001

declare cno cursor with return for select branch_id, branch_name from v10_test_tmp with ur;
CURSOR cno loop
      FETCH cno INTO branch_id_tmp, branch_name_tmp;
      
      sleep 1sec
      echo $branch_id_tmp, $branch_name_tmp
      
end loop

undeclare cno cursor

db connect reset

echo `pwd`/executesql.sql has over!

exit 0