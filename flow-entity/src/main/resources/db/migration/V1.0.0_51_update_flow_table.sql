-- mysql
CREATE INDEX flow_history_pre_id_IDX USING BTREE ON flow_history (pre_id);




-- postgresql
CREATE INDEX "flow_history_pre_id_IDX" ON "public"."flow_history" USING btree (
  "pre_id"
);