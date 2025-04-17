import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import LoggedInNavbar from "../LoggedInNavbar";
import Sidebar from "../Admin/Sidebar";
import Swal from "sweetalert2";

export default function AdminProfileUpdate() {
    const [formData, setFormData] = useState({ name: "", email: "" });
    const [error, setError] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        const fetchProfile = async () => {
            const token = localStorage.getItem("token");
            if (!token) {
                navigate("/");
                return;
            }
            try {
                const response = await axios.get("http://localhost:8081/admin/profile", {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setFormData({
                    name: response.data.name || "",
                    email: response.data.email || "",
                });
                console.log("Profile data fetched successfully");
            } catch (err) {
                Swal.fire({
                    icon: 'error',
                    title: 'Failed to fetch data',
                    text: err.message,
                    customClass: {
                        popup: 'my-swal-popup',
                        title: 'my-swal-title',
                        htmlContainer: 'my-swal-content',
                        confirmButton: 'my-swal-confirm-button',
                    }
                });

                setTimeout(() => navigate("/"), 2000);
                console.log("Error: ", err);
            }
        };
        fetchProfile();
    }, [navigate]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        const token = localStorage.getItem("token");
        if (!token) {
            navigate("/");
            return;
        }
        try {
            const response = await axios.put("http://localhost:8081/admin/update", formData,
                { headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" } }
            );

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
            navigate("/admin/profile");
            console.log("Profile updated successfully");
        } catch (err) {
            setError("Please log in again.");

            Swal.fire({
                icon: 'error',
                title: 'Profile Update Failed',
                text: error,
                customClass: {
                    popup: 'my-swal-popup',
                    title: 'my-swal-title',
                    htmlContainer: 'my-swal-content',
                    confirmButton: 'my-swal-confirm-button',
                }
            });
            setTimeout(() => navigate("/"), 2000);
            console.log("Error: ", err);
        }
    };

    return (
        <>
            {/* Navbar */}
            <LoggedInNavbar path={"/admin/profile/update"} />

            {/* Content */}
            <div className="container-fluid mt-8">
                <div className="row height">
                    {/* Sidebar */}
                    <Sidebar />
                    <main className="col-md-9 col-xl-10 ms-sm-auto">
                        <div className="shadow-lg rounded p-3 my-3">

                            <form onSubmit={handleSubmit}>
                                <div className="mb-3">
                                    <label className="form-label">Name</label>
                                    <input type="text" name="name" className="form-control" value={formData.name} onChange={handleChange} required />
                                </div>

                                <div className="mb-3">
                                    <label className="form-label">Email</label>
                                    <input type="email" name="email" className="form-control" value={formData.email} onChange={handleChange} required />
                                </div>
                                <div className="text-end my-3">
                                    <button type="submit" className="btn btn-123458">Save</button>
                                </div>
                            </form>
                        </div>
                    </main>
                </div >
            </div >
        </>
    );
};