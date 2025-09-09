# Question 1
Given the pointer to the head node of a linked list, change the next pointers of the nodes so that their order is reversed. The head pointer given may be null meaning that the initial list is empty.

## Example
references the list 1 -> 2 -> 3 -> NULL

Manipulate the  pointers of each node in place and return , now referencing the head of the list 3 -> 2 -> 1 -> NULL.

## Function Description

Complete the reverse function in the editor below.

reverse has the following parameter:

* SinglyLinkedListNode pointer head: a reference to the head of a list
## Returns

* SinglyLinkedListNode pointer: a reference to the head of the reversed list
## Input Format

The first line contains an integer , the number of test cases.

Each test case has the following format:

The first line contains an integer n, the number of elements in the linked list.
Each of the next n lines contains an integer, the  values of the elements in the linked list.

## Constraints
```
* 1 <= t <= 10
* 1 <= n <= 1000
* 1 <= list[i] <= 1000, where  is the  element in the list.
```
## Sample Input
```
1
5
1
2
3
4
5
```

## Sample Output
```
5 4 3 2 1 
```
## Explanation

The initial linked list is: ```1 -> 2 -> 3 -> 4 -> 5 -> NULL```.

The reversed linked list is: ```5 -> 4 -> 3 -> 2 -> 1 -> NULL```.


# Question 2

Given pointers to the heads of two sorted linked lists, merge them into a single, sorted linked list. Either head pointer may be null meaning that the corresponding list is empty.

## Example
headA refers to ```1 -> 3 -> 7 -> NULL```
headB refers to ```1 -> 2 -> NULL```

The new list is ```1 -> 1 -> 2 -> 3 -> 7 -> NULL```

## Function Description

Complete the mergeLists function in the editor below.

mergeLists has the following parameters:

* SinglyLinkedListNode pointer headA: a reference to the head of a list
* SinglyLinkedListNode pointer headB: a reference to the head of a list


## Returns

* SinglyLinkedListNode pointer: a reference to the head of the merged list


## Input Format

The first line contains an integer , the number of test cases.

The format for each test case is as follows:

The first line contains an integer , the length of the first linked list.
The next  lines contain an integer each, the elements of the linked list.
The next line contains an integer , the length of the second linked list.
The next  lines contain an integer each, the elements of the second linked list.

## Constraints
* 1 <= t <= 10
* 1 <= n, m <= 1000
* 1 <= list[i], where list[i] is the ith element of the list.

## Sample Input
```
1
3
1
2
3
2
3
4
```

## Sample Output
```
1 2 3 3 4 
```

## Explanation

The first linked list is: ``` 1 -> 3 -> 7 -> NULL```

The second linked list is: ``` 3 -> 4 -> NULL```

Hence, the merged linked list is: ```1 -> 2 -> 3 -> 3 -> 4 -> NULL```


# Question 3

Create a simple web application using one of the following programming languages and a framework of your choice. The requirements are as follows:

* Use a database for data persistence.
* Implement interfaces for creating posts and comments.
* Implement login functionality.
* Use Docker and Docker Compose for local deployment.
* Use a caching service.
* Create unit test for important code
* Create end to end test with test container
* Bonus: Include CI/CD scripts for deployment.
