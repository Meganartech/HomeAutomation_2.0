import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import LoggedInNavbar from "../LoggedInNavbar";
import Sidebar from "../User/Sidebar";
import Swal from "sweetalert2";

export default function AddThing() {
    const [formData, setFormData] = useState({ label: "", thingTypeUID: "", roomName: "", host: "" });
    const [thing, setThing] = useState([]);
    const [room, setRoom] = useState([]);
    const [error, setError] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        const fetchData = async () => {
            const token = localStorage.getItem("token");
            if (!token) {
                navigate("/");
                return;
            }
            try {
                const [thingResponse, roomResponse] = await Promise.all([
                    axios.get("http://localhost:8081/user/thing/list", { headers: { Authorization: `Bearer ${token}` } }),
                    axios.get("http://localhost:8081/user/room/list", { headers: { Authorization: `Bearer ${token}` } })
                ]);
                setThing(thingResponse.data);
                setRoom(roomResponse.data);
            } catch (err) {
                setError(err.response?.data?.error || "Please log in again");
                setTimeout(() => navigate("/"), 2000);
            }
        };
        fetchData();
    }, [navigate]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const token = localStorage.getItem("token");
        if (!token) {
            navigate('/');
            return;
        }
        try {
            const response = await axios.post("http://localhost:8081/user/thing", formData,
                { headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` } }
            );
            if (response.status === 200) {
                setError('');

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
                const newData = {
                    label: formData.label,
                    thingTypeUID: formData.thingTypeUID,
                    roomName: formData.roomName,
                    host: formData.host
                };
                setThing([...thing, newData]);
                setFormData({ thingTypeUID: "", label: "", roomName: "", host: "" });
                console.log("Thing added successfully");
            }
        } catch (err) {
            const errorMessage = err.response?.data?.error || "Failed to add thing";
            setError(typeof errorMessage === "string" ? errorMessage : JSON.stringify(errorMessage));
            console.error("Error:", err);
        }
    };

    return (
        <>
            {/* Navbar */}
            <LoggedInNavbar path={"/user/device"} />

            {/* Content */}
            <div className="container-fluid mt-8">
                <div className="row height">

                    {/* Sidebar */}
                    <Sidebar />
                    <main className="col-md-9 col-xl-10 ms-sm-auto">
                        <div className="shadow-lg p-3 bg-body rounded my-3">

                            {error && <div className="alert alert-danger my-2">{error}</div>}

                            <form onSubmit={handleSubmit}>
                                <div className="row">

                                    <div className="col-md-4 mb-3">
                                        <label className="form-label" htmlFor="roomName">Room</label>
                                        <select id="roomName" name="roomName" className="form-select" value={formData.roomName} onChange={handleChange} required>
                                            <option value="">--Select Room--</option>
                                            {room.map(roomData => (
                                                <option key={roomData.roomName} value={roomData.roomName}>
                                                    {roomData.roomName}
                                                </option>
                                            ))}
                                        </select>
                                    </div>


                                    <div className="col-md-4 mb-3">
                                        <label className="form-label" htmlFor="label">Device Name</label>
                                        <input type="text" id="label" name="label" className="form-control" value={formData.label} onChange={handleChange} required />
                                    </div>

                                    <div className="col-md-4 mb-3">
                                        <label className="form-label" htmlFor="host">Host</label>
                                        <input type="text" id="host" name="host" className="form-control" value={formData.host} onChange={handleChange} />
                                    </div>

                                    <div className="col-md-4 mb-3">
                                        <label className="form-label" htmlFor="thingTypeUID">Type</label>
                                        <select id="thingTypeUID" name="thingTypeUID" className="form-select" value={formData.thingTypeUID} onChange={handleChange} required>
                                            <option value="">--Select Type--</option>
                                            <option value="mqtt:topic">MQTT Device</option>
                                            <option value="zwave:device">Z-Wave Device</option>
                                            <option value="knx:device">KNX Device</option>
                                            <option value="network:pingdevice">Network Device</option>
                                        </select>
                                    </div>

                                    <div className="text-center mb-3">
                                        <button type="submit" className="btn btn-123458 btn-sm">Submit</button>
                                    </div>
                                </div>
                            </form>

                            <div className="table-responsive">
                                <table className="table table-bordered table-striped w-100">
                                    <thead>
                                        <tr>
                                            <th className="fw-bold bg-light border p-2">S.No</th>
                                            <th className="fw-bold bg-light border p-2">Device Name</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {thing.length > 0 ? (
                                            thing.map((data, index) => (
                                                <React.Fragment key={index}>
                                                    <tr>
                                                        <td className="p-2">{index + 1}</td>
                                                        <td className="p-2">
                                                            {data.label || "No device found"} <br />
                                                            <span className="badge bg-123458">{data.roomName || "No room name found"}</span>
                                                        </td>
                                                    </tr>
                                                </React.Fragment>
                                            ))
                                        ) : (
                                            <tr>
                                                <td colSpan={"2"} className="text-center">No device found</td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </main>
                </div>
            </div>
        </>
    );
}
