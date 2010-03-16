drop table if exists obd_data;

create table obd_data (

    lat double not null,
    lon double not null,
    air_temp double,
    intake_temp double,
    intake_press double,
    bar_press double,
    throttle_pos double,
    gspd double,
    vspd double,
    gtime double,
    otime double
);

