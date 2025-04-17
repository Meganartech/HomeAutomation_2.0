import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import LoggedInNavbar from "../LoggedInNavbar";
import Sidebar from "../User/Sidebar";
import Swal from "sweetalert2";

export default function AddRoom() {
    const [formData, setFormData] = useState({ roomName: "" });
    const [room, setRoom] = useState([]);
    const [error, setError] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        const fetchRoom = async () => {
            const token = localStorage.getItem("token");
            if (!token) {
                navigate("/");
                return;
            }
            try {
                const response = await axios.get("http://localhost:8081/user/room/list",
                    { headers: { Authorization: `Bearer ${token}` } }
                );
                setRoom(response.data);
            } catch (err) {
                Swal.fire({
                    icon: 'error',
                    title: 'Register Failed',
                    text: err.response?.data?.error || "Please log in again",
                    customClass: {
                        popup: 'my-swal-popup',
                        title: 'my-swal-title',
                        htmlContainer: 'my-swal-content',
                        confirmButton: 'my-swal-confirm-button',
                    }
                });
                setTimeout(() => navigate("/"), 2000);
            }
        };
        fetchRoom();
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
            const response = await axios.post("http://localhost:8081/user/room", formData, {
                headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}`, }
            });
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
                    roomName: formData.roomName
                };
                setRoom([...room, newData]);
                setFormData({ roomName: "" });
            }
        } catch (err) {
            Swal.fire({
                icon: 'error',
                title: error,
                text: err.response?.data?.error || "Failed to add room",
                customClass: {
                    popup: 'my-swal-popup',
                    title: 'my-swal-title',
                    htmlContainer: 'my-swal-content',
                    confirmButton: 'my-swal-confirm-button',
                }
            });
            console.error("Error:", err);
        }
    };

    return (
        <>
            {/* Navbar */}
            <LoggedInNavbar path={"/user/room"} />

            {/* Content */}
            <div className="container-fluid mt-8">
                <div className="row height">

                    {/* Sidebar */}
                    <Sidebar />
                    <main className="col-md-9 col-xl-10 ms-sm-auto">
                        <div className="shadow-lg p-3 bg-body rounded my-3">

                            {/* {error && <div className="alert alert-danger my-2">{error}</div>} */}

                            <form onSubmit={handleSubmit}>
                                <div className="row">
                                    <div className="col mb-3">
                                        <label className="form-label" htmlFor="roomName">Room Name</label>
                                        <input type="text" id="roomName" name="roomName" className="form-control" value={formData.roomName} onChange={handleChange} required />
                                    </div>

                                    <div className="text-center mb-3">
                                        <button type="submit" className="btn btn-123458 btn-sm">Add Room</button>
                                    </div>
                                </div>
                            </form>
                            <div className="table-responsive">
                                <table className="table table-bordered table-striped w-100">
                                    <thead>
                                        <tr>
                                            <th className="fw-bold bg-light border p-2">S.No</th>
                                            <th className="fw-bold bg-light border p-2">Room Name</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {room.length > 0 ? (
                                            room.map((data, index) => (
                                                <React.Fragment key={index}>
                                                    <tr>
                                                        <td className="p-2">{index + 1}</td>
                                                        <td className="p-2">{data.roomName || "No room name  found"}</td>
                                                    </tr>
                                                </React.Fragment>
                                            ))
                                        ) : (
                                            <tr>
                                                <td colSpan={"3"} className="text-center">No room found</td>
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