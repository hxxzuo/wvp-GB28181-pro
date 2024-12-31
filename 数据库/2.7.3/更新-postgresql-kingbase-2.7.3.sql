/*
* 20241222
*/

alter table wvp_device_channel
    drop index uk_wvp_device_channel_unique_device_channel;
alter table wvp_device_channel
    drop index uk_wvp_unique_stream_push_id;
alter table wvp_device_channel
    drop index uk_wvp_unique_stream_proxy_id;

alter table wvp_device_channel
    add data_type integer not null;

alter table wvp_device_channel
    add data_device_id integer not null;

update wvp_device_channel wdc
set data_type = 1, data_device_id = (SELECT device_db_id from wvp_device_channel where device_db_id is not null and id = wdc.id )  where device_db_id is not null;

update wvp_device_channel wdc
set data_type = 2, data_device_id = (SELECT stream_push_id from wvp_device_channel where stream_push_id is not null and id = wdc.id )  where stream_push_id is not null;

update wvp_device_channel wdc
set data_type = 3, data_device_id = (SELECT stream_proxy_id from wvp_device_channel where stream_proxy_id is not null and id = wdc.id )  where stream_proxy_id is not null;

alter table wvp_device_channel drop device_db_id;
alter table wvp_device_channel drop stream_push_id;
alter table wvp_device_channel drop stream_proxy_id;
