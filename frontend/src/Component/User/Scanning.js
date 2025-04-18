import React, { useState, useEffect } from "react";
import axios from "axios";
import LoggedInNavbar from "../LoggedInNavbar";
import Sidebar from "../User/Sidebar";
import Swal from "sweetalert2";

export default function DeviceScanner() {
    const [room, setRoom] = useState([]);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [inboxDevices, setInboxDevices] = useState([]);
    const [selectedRooms, setSelectedRooms] = useState({});

    useEffect(() => {
        const fetchRoom = async () => {
            const token = localStorage.getItem("token");
            if (!token) {
                setError("Authentication token missing. Please log in again.");
                return;
            }

            try {
                const response = await axios.get("http://localhost:8081/user/room/list", {
                    headers: { Authorization: `Bearer ${token}` }
                });
                console.log("Room List Response:", response.data);
                if (response.status === 200) {
                    setRoom(response.data);
                }
            } catch (err) {
                const errorMsg = err.response?.data?.error || "Failed to fetch rooms";
                setError(errorMsg);
            }
        }
        fetchRoom();
    }, []);


    const handleScan = async () => {
        const token = localStorage.getItem("token");
        if (!token) {
            setError("Authentication token missing. Please log in again.");
            return;
        }

        try {
            const response = await axios.post("http://localhost:8081/user/scan", {}, {
                headers: { Authorization: `Bearer ${token}` }
            });

            if (response.status === 200) {
                await Swal.fire({
                    icon: 'success',
                    text: response.data.message,
                    showConfirmButton: false,
                    timer: 1500,
                    customClass: {
                        popup: 'my-swal-popup',
                        title: 'my-swal-title',
                        htmlContainer: 'my-swal-content',
                        confirmButton: 'my-swal-confirm-button',
                    }
                });
                setError("");
                fetchInboxDevices();
            }
        } catch (err) {
            const errorMsg = err.response?.data?.error || "Failed to trigger device scan";
            setError(errorMsg);
            setMessage("");
        }
    };

    const fetchInboxDevices = async () => {
        const token = localStorage.getItem("token");
        if (!token) {
            setError("Authentication token missing. Please log in again.");
            return;
        }

        try {
            const response = await axios.get("http://localhost:8081/user/inbox", {
                headers: { Authorization: `Bearer ${token}` }
            });

            if (response.status === 200) {
                setInboxDevices(response.data);
                setError("");
            }
        } catch (err) {
            const errorMsg = err.response?.data?.error || "Failed to fetch inbox devices";
            setError(errorMsg);
        }
    };

    const handleRoomChange = (index, value) => {
        setSelectedRooms(prevState => ({
            ...prevState,
            [index]: value
        }));
    };

    const handleAddDevice = async (data, index) => {
        const token = localStorage.getItem("token");
        if (!token) {
            setError("Authentication token missing. Please log in again.");
            return;
        }

        const selectedRoom = selectedRooms[index];
        if (!selectedRoom) {

            await Swal.fire({
                icon: 'warning',
                text: 'Please select a room',
                showConfirmButton: false,
                timer: 1500,
                customClass: {
                    popup: 'my-swal-popup',
                    title: 'my-swal-title',
                    htmlContainer: 'my-swal-content',
                    confirmButton: 'my-swal-confirm-button',
                }
            });
            return;
        }
        // Now send the selectedRoom along with device data
        console.log("Device Data:", data);
        console.log("Selected Room:", selectedRoom);

        // Build the request payload
        const requestBody = {
            thingTypeUID: data.thingTypeUID,
            label: data.label,
            thingUID: data.thingUID, // If exists in your inbox data
            host: data.host, // Pass host explicitly
            roomName: selectedRoom, // TODO: Replace with selected room name from user input
        };

        try {
            const response = await axios.post("http://localhost:8081/user/thing", requestBody, {
                headers: { Authorization: `Bearer ${token}` }
            });

            if (response.status === 200) {

                await Swal.fire({
                    icon: 'success',
                    text: response.data.message,
                    showConfirmButton: false,
                    timer: 1500,
                    customClass: {
                        popup: 'my-swal-popup',
                        title: 'my-swal-title',
                        htmlContainer: 'my-swal-content',
                        confirmButton: 'my-swal-confirm-button',
                    }
                });

                setError("");
                fetchInboxDevices(); // Refresh the list if needed
            }
        } catch (err) {
            const errorMsg = err.response?.data?.error || "Failed to add device";
            setError(errorMsg);
            setMessage("");
        }
    };

    return (

        <>
            {/* Navbar */}
            <LoggedInNavbar path={"/user/scan"} />

            {/* Content */}
            <div className="container-fluid mt-8">
                <div className="row height">

                    {/* Sidebar */}
                    <Sidebar />
                    <main className="col-md-9 col-xl-10 ms-sm-auto">
                        <div className="shadow-lg p-3 bg-body rounded my-3">

                            {error && <div className="alert alert-danger my-2">{error}</div>}

                            {message && <div className="alert alert-success my-2">{message}</div>}

                            <div className="text-center">
                                <button className="btn btn-123458 btn-sm mb-3" onClick={handleScan}>
                                    Scan for Devices
                                </button>
                            </div>

                            <div className="table-responsive">
                                <table className="table table-bordered table-striped w-100">
                                    <thead>
                                        <tr>
                                            <th className="fw-bold bg-light border p-2">S.No</th>
                                            <th className="fw-bold bg-light border p-2">Device Name</th>
                                            <th className="fw-bold bg-light border p-2">Device Type</th>
                                            <th className="fw-bold bg-light border p-2">Room</th>
                                            <th className="fw-bold bg-light border p-2">Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {inboxDevices.length > 0 ? (
                                            inboxDevices.map((data, index) => (
                                                <React.Fragment key={index}>
                                                    <tr>
                                                        <td className="p-2">{index + 1}</td>
                                                        <td className="p-2">{data?.label || "No device found"}</td>
                                                        <td className="p-2">{data?.thingTypeUID || "No device found"}</td>
                                                        <td className="p-2">
                                                            <select className="form-select" onChange={(e) => handleRoomChange(index, e.target.value)} defaultValue="">
                                                                <option value="" disabled>Select Room</option>
                                                                {room.map((room) => (
                                                                    <option key={room.roomId} value={room.roomName}>
                                                                        {room.roomName}
                                                                    </option>
                                                                ))}
                                                            </select>
                                                        </td>
                                                        <td className="p-2">
                                                            <button className="btn btn-123458 btn-sm"
                                                                onClick={() => handleAddDevice(data, index)}>Add Device</button>
                                                        </td>
                                                    </tr>
                                                </React.Fragment>
                                            ))
                                        ) : (
                                            <tr>
                                                <td colSpan={"5"} className="text-center">No device found</td>
                                            </tr>
                                        )}
                                    </tbody>

                                </table>
                            </div>
                        </div>
                    </main>
                </div >
            </div >
        </>
    );
}
