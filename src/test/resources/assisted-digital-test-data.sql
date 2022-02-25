-- #########################################
-- DATA SETUP
-- 2021-12-20:
-- 1 assisted claim with 2 booking status, NEW_CLAIM and SUCCESS
-- 1 online claim with 1 booking status SUCCESS

-- 2021-12-21:
-- 3 assisted claim
--    - 1st: 1 booking status of NEW_CLAIM
--    - 2nd: 2 booking status of NEW_CLAIM and SUCCESS
--    - 3rd: 2 booking status of NEW_CLAIM, FIRST_FAIL
-- 1 online claim with 1 booking status SUCCESS

-- 2021-12-22:
-- 1 assisted claim (from 2021-12-21)
--    - 3rd: 1 booking status of SUCCESS
-- #########################################


-- #########################################
-- CLAIM STATUS - 2021-12-20
-- #########################################
-- None assisted
INSERT INTO claim_status (id, claimant_id, locked, created_timestamp, updated_timestamp, hash, is_duplicate) VALUES ('b4bbf898-5588-411f-a6ff-da4d8dfce57f', 'ead17a02-fe13-41b9-95f7-59b1b13176f8', false, '2021-12-20 15:48:13.833', '2021-12-20 15:48:13.833', 'afa2fc7f1837cc704a5a628fa30b71dc3d923b547b01de6d7069cb333df659dc', false);
-- Assisted with multiple booking status on same day
INSERT INTO claim_status (id, claimant_id, locked, created_timestamp, updated_timestamp, hash, is_duplicate) VALUES ('e94e2c2a-276e-4cef-954b-b6e4debee92c', '78bf4a47-5fe6-4247-9944-f2170f735dfb', false, '2021-12-20 16:07:53.169', '2021-12-20 16:07:53.169', '3a53e1499ebb5e9ea1a77059463847bb6b60e294b28a993ee2a7d96f834d91a4', false);

-- #########################################
-- BOOKING STATUS - 2021-12-20
-- #########################################
-- None assisted
INSERT INTO booking_status (id, claim_status_id, status, substatus, job_centre_code, agent, created_timestamp, hash) VALUES ('6047daa1-6536-4e9b-92bc-55e979b7fb66', 'b4bbf898-5588-411f-a6ff-da4d8dfce57f', 'NEW_CLAIM', NULL, '205773', NULL, '2021-12-20 15:48:13.905', 'f72d5d1f2889fd93dc24f7eee3ce3e791c839bcc4bb9c683b5b99d9104f2030f');
-- Assisted with initial NEW_CLAIM status
INSERT INTO booking_status (id, claim_status_id, status, substatus, job_centre_code, agent, created_timestamp, hash) VALUES ('2f2575f0-c136-4884-a04e-52aa63110f23', 'e94e2c2a-276e-4cef-954b-b6e4debee92c', 'NEW_CLAIM', NULL, '205773', '00000001', '2021-12-20 16:07:53.176', 'fbeb5895dd61edc3180cde7fdd87af0ea8bc512d5aa9ff215fadd0ac3ec55ecd');
-- Assisted with updated SUCCESS status on same day as NEW_CLAIM
INSERT INTO booking_status (id, claim_status_id, status, substatus, job_centre_code, agent, created_timestamp, hash) VALUES ('1182c748-e2ac-428a-b1be-53a0f1ddafe2', 'e94e2c2a-276e-4cef-954b-b6e4debee92c', 'SUCCESS', '', '', '00000001', '2021-12-20 16:10:08.849', '056789114f6922c14a29c6f8b751a1110997496f5cfc5f99c257d4a634e9482f');

-- #########################################
-- CLAIM STATUS - 2021-12-21
-- #########################################
-- None assisted
INSERT INTO claim_status (id, claimant_id, locked, created_timestamp, updated_timestamp, hash, is_duplicate) VALUES ('b4bbf898-5588-411f-a6ff-da4d8dfce111', 'ead17a02-fe13-41b9-95f7-59b1b1317111', false, '2021-12-21 15:48:13.833', '2021-12-21 15:48:13.833', 'afa2fc7f1837cc704a5a628fa30b71dc3d923b547b01de6d7069cb333df659dc', false);
-- Assisted with single booking status on same day
INSERT INTO claim_status (id, claimant_id, locked, created_timestamp, updated_timestamp, hash, is_duplicate) VALUES ('e94e2c2a-276e-4cef-954b-b6e4debee222', '78bf4a47-5fe6-4247-9944-f2170f735222', false, '2021-12-21 16:07:53.169', '2021-12-21 16:07:53.169', '3a53e1499ebb5e9ea1a77059463847bb6b60e294b28a993ee2a7d96f834d91a4', false);
-- Assisted with two booking status on same day
INSERT INTO claim_status (id, claimant_id, locked, created_timestamp, updated_timestamp, hash, is_duplicate) VALUES ('e94e2c2a-276e-4cef-954b-b6e4debee333', '78bf4a47-5fe6-4247-9944-f2170f735333', false, '2021-12-21 16:07:53.169', '2021-12-21 16:07:53.169', '3a53e1499ebb5e9ea1a77059463847bb6b60e294b28a993ee2a7d96f834d91a4', false);
-- Assisted with three booking status on same day
INSERT INTO claim_status (id, claimant_id, locked, created_timestamp, updated_timestamp, hash, is_duplicate) VALUES ('e94e2c2a-276e-4cef-954b-b6e4debee444', '78bf4a47-5fe6-4247-9944-f2170f735444', false, '2021-12-21 16:07:53.169', '2021-12-22 16:07:53.169', '3a53e1499ebb5e9ea1a77059463847bb6b60e294b28a993ee2a7d96f834d91a4', false);

-- #########################################
-- BOOKING STATUS - 2021-12-21
-- #########################################
-- None assisted
INSERT INTO booking_status (id, claim_status_id, status, substatus, job_centre_code, agent, created_timestamp, hash) VALUES ('6047daa1-6536-4e9b-92bc-55e979b7f222', 'b4bbf898-5588-411f-a6ff-da4d8dfce111', 'NEW_CLAIM', NULL, '205773', NULL, '2021-12-21 15:48:13.905', 'f72d5d1f2889fd93dc24f7eee3ce3e791c839bcc4bb9c683b5b99d9104f2030f');
-- Assisted with single NEW_CLAIM status
INSERT INTO booking_status (id, claim_status_id, status, substatus, job_centre_code, agent, created_timestamp, hash) VALUES ('2f2575f0-c136-4884-a04e-52aa63110333', 'e94e2c2a-276e-4cef-954b-b6e4debee222', 'NEW_CLAIM', NULL, '205773', '00000001', '2021-12-21 16:07:53.176', 'fbeb5895dd61edc3180cde7fdd87af0ea8bc512d5aa9ff215fadd0ac3ec55ecd');
-- Assisted with two status
INSERT INTO booking_status (id, claim_status_id, status, substatus, job_centre_code, agent, created_timestamp, hash) VALUES ('2f2575f0-c136-4884-a04e-52aa63110444', 'e94e2c2a-276e-4cef-954b-b6e4debee333', 'NEW_CLAIM', NULL, '205773', '00000001', '2021-12-21 16:07:53.176', 'fbeb5895dd61edc3180cde7fdd87af0ea8bc512d5aa9ff215fadd0ac3ec55ecd');
INSERT INTO booking_status (id, claim_status_id, status, substatus, job_centre_code, agent, created_timestamp, hash) VALUES ('2f2575f0-c136-4884-a04e-52aa63110555', 'e94e2c2a-276e-4cef-954b-b6e4debee333', 'SUCCESS', NULL, '205773', '00000001', '2021-12-21 16:07:53.176', 'fbeb5895dd61edc3180cde7fdd87af0ea8bc512d5aa9ff215fadd0ac3ec55ecd');
-- Assisted with three status
INSERT INTO booking_status (id, claim_status_id, status, substatus, job_centre_code, agent, created_timestamp, hash) VALUES ('2f2575f0-c136-4884-a04e-52aa63110666', 'e94e2c2a-276e-4cef-954b-b6e4debee444', 'NEW_CLAIM', NULL, '205773', '00000001', '2021-12-21 16:07:53.176', 'fbeb5895dd61edc3180cde7fdd87af0ea8bc512d5aa9ff215fadd0ac3ec55ecd');
INSERT INTO booking_status (id, claim_status_id, status, substatus, job_centre_code, agent, created_timestamp, hash) VALUES ('2f2575f0-c136-4884-a04e-52aa63110777', 'e94e2c2a-276e-4cef-954b-b6e4debee444', 'FIRST_FAIL', NULL, '205773', '00000001', '2021-12-21 16:07:53.176', 'fbeb5895dd61edc3180cde7fdd87af0ea8bc512d5aa9ff215fadd0ac3ec55ecd');

-- #########################################
-- BOOKING STATUS - 2021-12-22
-- #########################################
INSERT INTO booking_status (id, claim_status_id, status, substatus, job_centre_code, agent, created_timestamp, hash) VALUES ('2f2575f0-c136-4884-a04e-52aa63110888', 'e94e2c2a-276e-4cef-954b-b6e4debee444', 'SUCCESS', NULL, '205773', '00000001', '2021-12-22 16:07:53.176', 'fbeb5895dd61edc3180cde7fdd87af0ea8bc512d5aa9ff215fadd0ac3ec55ecd');
