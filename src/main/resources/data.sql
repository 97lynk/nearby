insert into account(id, name, age)
values ('tuan', 'Nguyen Tuan', 10),
       ('duyen', 'Nguyen Duyen', 10),
       ('tony', 'Tony Nguyen', 11)
;

insert into friendship(account_id, friend_id, type, start_time)
values ('tuan', 'duyen', 'love', now()),
       ('tuan', 'tony', 'friend', now()),
       ('tony', 'tuan', 'friend', now()),
       ('duyen', 'tuan', 'friend', now())
;